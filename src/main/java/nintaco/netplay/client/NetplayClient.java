package nintaco.netplay.client;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import nintaco.*;
import nintaco.apu.*;
import nintaco.files.*;
import nintaco.gui.*;
import nintaco.gui.InformationDialog.*;
import nintaco.gui.netplay.client.*;
import nintaco.input.*;
import nintaco.input.other.*;
import nintaco.mappers.nintendo.vs.*;
import nintaco.movie.*;
import nintaco.netplay.protocol.*;
import nintaco.netplay.queue.*;
import nintaco.palettes.*;
import nintaco.preferences.*;
import nintaco.util.*;

import static java.nio.charset.StandardCharsets.*;
import static nintaco.files.FileType.*;
import static nintaco.gui.netplay.client.ClientStatus.*;
import static nintaco.netplay.protocol.Protocol.*;
import static nintaco.netplay.client.ClientState.*;
import static nintaco.netplay.protocol.MessageType.*;
import static nintaco.util.BitUtil.*;
import static nintaco.util.CollectionsUtil.*;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.ThreadUtil.*;

public class NetplayClient implements Runnable {

    private static final int RETRY_SECONDS = 5;
    private static final long RETRY_MILLIS = TimeUnit.SECONDS
            .toMillis(RETRY_SECONDS);
    private static final long HEARTBEAT_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private final Object writeMonitor = new Object();
    private final Object stateMonitor = new Object();
    private final RingQueue sendQueue = new RingQueue();
    private final RingQueue receiveQueue = new RingQueue();
    private final QueueElement receiveElement = new QueueElement();

    private Thread heartbeatThread;
    private Thread readMessageThread;
    private Thread sendThread;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ClientState state;
    private Window window;
    private boolean spectator;
    private boolean wasTrackingHistory;

    private volatile Thread mainThread;
    private volatile NetplayClientPrefs prefs;
    private volatile char[] password;
    private volatile byte[] salt;
    private volatile boolean quickSavesEnabled;
    private volatile int playerResponse;
    private volatile int receiveTail;
    private volatile boolean running;
    private volatile boolean retryImmediately;
    private volatile String[] quickSaveNames;

    void addActivity(final String activity, final Object... params) {
        final NetplayClientFrame clientFrame = App.getNetworkClientFrame();
        if (clientFrame != null) {
            clientFrame.addActivity(activity, params);
        }
    }

    void setClientStatus(final ClientStatus status) {
        final NetplayClientFrame clientFrame = App.getNetworkClientFrame();
        if (clientFrame != null) {
            clientFrame.setClientStatus(status);
        }
    }

    private NetplayClientFrame getClientFrame() {
        final NetplayClientFrame clientFrame = App.getNetworkClientFrame();
        if (clientFrame != null && !clientFrame.isVisible()) {
            clientFrame.setVisible(true);
        }
        if (clientFrame != null) {
            clientFrame.toFront();
        }
        return clientFrame;
    }

    public boolean isRunning() {
        return running;
    }

    public void start(final char[] password) {

        if (mainThread != null) {
            stop();
        }
        this.password = password;

        mainThread = new Thread(this, "Netplay Client Main Thread");
        mainThread.start();
    }

    @Override
    public void run() {
        addActivity("Client started.");

        final AppPrefs appPrefs = AppPrefs.getInstance();

        wasTrackingHistory = appPrefs.getHistoryPrefs().isTrackHistory();
        if (!wasTrackingHistory) {
            appPrefs.getHistoryPrefs().setTrackHistory(true);
            App.setTrackHistory(true);
        }

        prefs = appPrefs.getNetplayClientPrefs();
        spectator = prefs.getPlayer() >= 4;
        App.dispose();
        overrideInput();

        running = true;
        while (running) {
            setClientStatus(CONNECTING);
            outer:
            try {
                try {
                    addActivity("Connecting to %s:%d...", prefs.getHost(),
                            prefs.getPort());
                    socket = new Socket(prefs.getHost(), prefs.getPort());
                } catch (final Throwable t) {
                    addActivity("Failed to establish connection.");
                    break outer;
                }

                addActivity("Connection established.");
                setClientStatus(ONLINE);
                state = SLEEP;

                out = new DataOutputStream(new BufferedOutputStream(
                        socket.getOutputStream()));
                in = new DataInputStream(new BufferedInputStream(
                        socket.getInputStream()));

                heartbeatThread = new Thread(this::heartbeatLoop,
                        "Netplay Client Hearbeat Thread");
                heartbeatThread.start();
                readMessageThread = new Thread(this::readMessageLoop,
                        "Netplay Client Read Message Thread");
                readMessageThread.start();
                sendThread = new Thread(this::sendLoop, "Netplay Client Send Thread");
                sendThread.start();

                inner:
                while (running) {
                    final ClientState s;
                    synchronized (stateMonitor) {
                        if (state == DISCONNECTED) {
                            addActivity("Disconnected.");
                            break inner;
                        }
                        s = state;
                        state = SLEEP;
                    }
                    switch (s) {
                        case SLEEP:
                            synchronized (stateMonitor) {
                                while (state == SLEEP) {
                                    threadWait(stateMonitor);
                                }
                            }
                            break;
                        case AUTHENTICATE:
                            sendPasswordHash();
                            break;
                        case REQUEST_PLAYER:
                            sendRequestPlayer();
                            break;
                        case CHOOSE_PLAYER:
                            choosePlayer();
                            break;
                        case REQUEST_FILE:
                            sendRequestFile();
                            break;
                        case DISCONNECTED:
                            break inner;
                    }
                }

            } catch (final Throwable t) {
//        t.printStackTrace();
                addActivity("Disconnected.");
                setState(DISCONNECTED);
            } finally {
                closeSocket();
                EDT.async(() -> {
                    if (window != null) {
                        window.dispose();
                        window = null;
                    }
                });
            }

            if (running) {
                setClientStatus(CONNECTING);
                if (retryImmediately) {
                    addActivity("Will retry immediately...");
                } else {
                    addActivity("Will retry in %d seconds...", RETRY_SECONDS);
                }
                sendQueue.stop();
                receiveQueue.stop();
                App.dispose();
                joinAll(heartbeatThread, readMessageThread, sendThread);
                if (retryImmediately) {
                    retryImmediately = false;
                } else {
                    sleep(RETRY_MILLIS);
                }
            }
        }

        InputUtil.clearOverrides();
        sendQueue.stop();
        receiveQueue.stop();
        App.dispose();
        joinAll(heartbeatThread, readMessageThread, sendThread);

        if (!wasTrackingHistory) {
            appPrefs.getHistoryPrefs().setTrackHistory(false);
            App.setTrackHistory(false);
        }

        addActivity("Client stopped.");
        setClientStatus(OFFLINE);
    }

    private void overrideInput() {
        InputUtil.setPortDeviceOverrides(spectator ? new int[0][0]
                : new int[][]{{prefs.getPlayer(), prefs.getInputDevice()}});
    }

    private void heartbeatLoop() {
        try {
            while (running) {
                send(Heartbeat);
                sleep(HEARTBEAT_MILLIS);
            }
        } catch (final Throwable t) {
//      t.printStackTrace();
            breakConnection();
        }
    }

    private void readMessageLoop() {
        try {
            while (running) {
                handleMessage(in.readUnsignedByte());
            }
        } catch (final Throwable t) {
//      t.printStackTrace();
            breakConnection();
        }
    }

    private void handleMessage(final int messageType) throws Throwable {
        switch (messageType) {
            case Heartbeat:
                break;
            case ServerDescription:
                handleServerDescription();
                break;
            case WrongPasswordError:
                handleWrongPasswordError();
                break;
            case Authenticated:
                addActivity("User authenticated.");
                setState(REQUEST_PLAYER);
                break;
            case PlayerResponse:
                handlePlayerResponse();
                break;
            case FileResponse:
                handleFileResponse();
                break;
            case NoFileResponse:
                handleNoFileResponse();
                break;
            case ControllerInput:
                handleControllerInput();
                break;
            case SaveState:
                handleSaveState();
                break;
            case Play:
                handlePlay();
                break;
            case Rewind:
                handleRewind();
                break;
            case MovieBlock:
                handleMovieBlock();
                break;
            case FrameEnd:
                handleFrameEnd();
                break;
            case QuickSaveStateMenuNames:
                handleQuickSaveStateMenuNames();
                break;
            case ShowMessage:
                handleShowMessage();
                break;
            default:
                addActivity("Received unknown message type: %d.", messageType);
                breakConnection();
                break;
        }
    }

    private void sendLoop() {
        try {
            final QueueElement element = new QueueElement();
            sendQueue.start();
            int tail = sendQueue.getHead();
            while (running) {
                if (sendQueue.consume(tail++, element)) {
                    synchronized (writeMonitor) {
                        out.write(element.messageType);
                        switch (element.dataType) {
                            case INTEGER:
                                out.writeInt(element.value);
                                break;
                            case BYTES:
                                writeRawByteArray(out, element.data);
                                break;
                            case ALL:
                                out.writeInt(element.value);
                                writeRawByteArray(out, element.data);
                                break;
                        }
                        out.flush();
                    }
                } else {
                    addActivity("Send underflow.");
                    addActivity("Breaking connection...");
                    breakConnection();
                    break;
                }
            }
        } catch (final Throwable t) {
//      t.printStackTrace();
            addActivity("Send transmission error.");
            addActivity("Breaking connection...");
            breakConnection();
        } finally {
            sendQueue.stop();
        }
    }

    private void handleControllerInput() throws Throwable {
        receiveQueue.produce(ControllerInput, in.readInt(), readRawByteArray(in));
    }

    private void handlePlay() throws Throwable {
        receiveQueue.produce(Play);
    }

    private void handleRewind() throws Throwable {
        receiveQueue.produce(Rewind, in.readInt());
    }

    private void handleMovieBlock() throws Throwable {
        receiveQueue.produce(MovieBlock, readRawByteArray(in));
    }

    private void handleFrameEnd() throws Throwable {
        receiveQueue.produce(FrameEnd);
    }

    private void handleQuickSaveStateMenuNames() throws Throwable {
        handleQuickSaveStateMenuNames((String[]) readObject(readRawByteArray(in)));
    }

    private void handleQuickSaveStateMenuNames(
            final String[] quickSaveStateMenuNames) {
        quickSaveNames = quickSaveStateMenuNames;
        EDT.async(() -> App.getImageFrame()
                .handleQuickSaveStateMenuNames(quickSaveStateMenuNames,
                        quickSavesEnabled));
    }

    private void handleSaveState() throws Throwable {
        addActivity("Save state received.");
        final IFile file = App.getFile();
        if (file == null) {
            addActivity("File closed.");
            addActivity("Breaking connection...");
            breakConnection();
            return;
        }
        receiveQueue.stop();
        App.dispose();
        final Machine machine = (Machine) readObject(readRawByteArray(in));
        final Ports ports = machine.getMapper().getPorts();
        InputUtil.setOverrides(ports.getConsoleType(), ports.isMultitap());
        InputUtil.setVsGame(null);
        if (file.getFileType() == NES) {
            final VsGame vsGame = ((NesFile) file).getVsGame();
            if (vsGame != null) {
                InputUtil.setVsGame(vsGame);
            }
        }
        final MachineRunner machineRunner = new MachineRunner(machine);
        new SetupROB(machine.getPPU().getRob()).run(machine);
        App.loadState(file, machineRunner);
        machineRunner.setClient(this);
        receiveTail = 0;
        receiveQueue.start();
        new Thread(machineRunner).start();
    }

    private void handleFileResponse() throws Throwable {
        final FileResponse fileResponse = (FileResponse) readObject(
                readRawByteArray(in));
        final IFile file = fileResponse.getFile();
        receiveQueue.stop();
        App.dispose();
        final MachineRunner machineRunner = new MachineRunner(
                fileResponse.getMachine());
        addActivity("File data and save state received.");
        machineRunner.setClient(this);
        final Movie movie = new Movie((file.getFileType() == NES)
                && ((NesFile) file).isVsDualSystem());
        movie.frameIndex = fileResponse.getMovieFrameIndex();
        machineRunner.setMovie(movie);
        machineRunner.setForwardTime(fileResponse.isForwardTime());
        machineRunner.setCurrentMovieBlock(fileResponse.getCurrentMovieBlock());
        machineRunner.setMovieBlock(fileResponse.getMovieBlock());
        SystemAudioProcessor.setMovie(movie);
        App.setMachineRunner(machineRunner);
        new SetupROB(fileResponse.getMachine().getPPU().getRob()).run(
                fileResponse.getMachine());
        App.loadState(file, machineRunner);
        App.getImageFrame().updateContentPane(fileResponse.getMachine().getMapper(),
                file.getFileType() == NSF ? (NsfFile) file : null);
        InputUtil.setOverrides(fileResponse.getConsole(),
                fileResponse.isMultitap());
        InputUtil.setVsGame(null);
        if (file.getFileType() == NES) {
            final NesFile nesFile = (NesFile) file;
            final VsGame vsGame = nesFile.getVsGame();
            final boolean paletteUpdated;
            if (vsGame != null) {
                InputUtil.setVsGame(vsGame);
                paletteUpdated = PaletteUtil.setVsPPU(vsGame.getPPU());
                if (vsGame.isDualSystemGame()) {
                    App.createSubMonitorFrame();
                }
            } else if (nesFile.isPlaychoice10()) {
                paletteUpdated = PaletteUtil.usePlayChoice10PPU();
            } else {
                paletteUpdated = PaletteUtil.setPalettePPU(PalettePPU._2C02);
            }
            if (paletteUpdated) {
                App.getImageFrame().createPaletteMenu();
            }
        }
        handleQuickSaveStateMenuNames(fileResponse.getQuickSaveStateMenuNames());
        App.getImageFrame().setFileInfo(fileResponse.getFileInfo());
        InputUtil.handleSettingsChange();
        receiveTail = 0;
        receiveQueue.start();
        new Thread(machineRunner).start();
        addActivity("Acknowledged received data.");
        sendQueue.produce(FileReceived, fileResponse.getFileRequestID());
    }

    private void handleNoFileResponse() throws Throwable {
        addActivity("File closed message received.");
        receiveQueue.stop();
        App.dispose();
    }

    private void handlePlayerResponse() throws Throwable {
        playerResponse = in.readUnsignedByte();
        if (playerResponse == PlayerGranted) {
            final int player = prefs.getPlayer();
            if (player < 4) {
                addActivity("User accepted as player %d.", player + 1);
            } else {
                addActivity("User accepted as spectator.");
            }
            setState(REQUEST_FILE);
        } else {
            addActivity("User player request denied.");
            setState(CHOOSE_PLAYER);
        }
    }

    private void handleServerDescription() throws Throwable {
        final String version = in.readUTF();
        salt = readRawByteArray(in);
        quickSavesEnabled = in.readBoolean() && !spectator;

        final boolean passwordRequired = !isBlank(salt);

        addActivity("Received Netplay Server description:");
        addActivity("* Version: %s", version);
        addActivity("* Password: %srequired", passwordRequired ? "" : "not ");
        addActivity("* Quick saves: %s", quickSavesEnabled ? "enabled"
                : spectator ? "disabled (spectator)" : "disabled");

        if (!VersionUtil.getVersion().equals(version)) {
            addActivity("Incompatible software versions detected.");
            kill();
            EDT.async(() -> displayIncompatibilityError(version));
        } else if (passwordRequired) {
            setState(AUTHENTICATE);
        } else {
            setState(REQUEST_PLAYER);
        }
    }

    private void handleShowMessage() throws Throwable {
        final byte[] data = readRawByteArray(in);
        if (data != null) {
            App.getImageFrame().getImagePane().showMessage(new String(data,
                    ISO_8859_1));
        }
    }

    private void handleWrongPasswordError() {
        addActivity("Invalid password.");
        prefs.setPasswordHash(null);
        AppPrefs.save();
        displayPasswordPrompt(false);
    }

    private void displayIncompatibilityError(final String serverVersion) {
        window = new InformationDialog(getClientFrame(), String.format("<html>"
                + "The Netplay Server is using a different version of this software.<br/>"
                + "Synchronize versions to establish a connection.<br/><br/>"
                + "<code>Server version: %s</code><br/>"
                + "<code>Client version: %s</code>"
                + "</html>", serverVersion, VersionUtil.getVersion()),
                "Incompatible Versions", IconType.ERROR);
        window.setVisible(true);
        window = null;
    }

    private int getSinglePlayerIndex() {
        switch (playerResponse) {
            case 0x01:
                return 0;
            case 0x02:
                return 1;
            case 0x04:
                return 2;
            case 0x08:
                return 3;
            case 0x10:
                return 4;
            default:
                return -1;
        }
    }

    private void displayAccessDenied(final NetplayClientFrame clientFrame) {
        addActivity("Netplay server is not accepting remote users.");
        window = new InformationDialog(clientFrame,
                "The Netplay Server is not accepting remote players or spectators at "
                        + "this time.", "Access Denied", IconType.ERROR);
        window.setVisible(true);
        window = null;
        synchronized (stateMonitor) {
            if (state == DISCONNECTED) {
                return;
            }
        }
        kill();
    }

    private void displayMultiplePlayersPrompt(final NetplayClientFrame clientFrame) {
        addActivity("User asked to be a different player.");
        final List<Integer> playerIndices = new ArrayList<>();
        final List<String> playerNames = new ArrayList<>();
        final int selectedPlayer = prefs.getPlayer();
        final int player = selectedPlayer + 1;
        for (int i = 0; i < 5; i++) {
            if (getBitBool(playerResponse, i)) {
                playerIndices.add(i);
                playerNames.add(i < 4 ? String.format("Player %d", i + 1)
                        : "Spectator");
            }
        }
        final ComboBoxDialog dialog = new ComboBoxDialog(clientFrame, "<html>"
                + (selectedPlayer < 4 ? "Player " + player + " is not available."
                : "The Netplay Server is not accepting spectators at this time.")
                + "<br/><br/>Select an alternative from the list below.</html>",
                "Choose Player", playerNames);
        window = dialog;
        dialog.setVisible(true);
        synchronized (stateMonitor) {
            if (state == DISCONNECTED) {
                window = null;
                return;
            }
        }
        final int inputIndex = dialog.getInputIndex();
        if (dialog.isOk() && inputIndex >= 0) {
            addActivity("User selected %s.", playerNames.get(inputIndex));
            prefs.setPlayer(playerIndices.get(inputIndex));
            AppPrefs.save();
            if (clientFrame != null) {
                clientFrame.loadFields();
            }
            setState(REQUEST_PLAYER);
        } else {
            addActivity("User declined change of player.");
            kill();
        }
        window = null;
    }

    private void displaySinglePlayerPrompt(final NetplayClientFrame clientFrame,
                                           final int playerIndex) {
        final YesNoDialog dialog;
        if (playerIndex < 4) {
            final int player = playerIndex + 1;
            addActivity("User asked to be player %d.", player);
            if (prefs.getPlayer() == 4) {
                dialog = new YesNoDialog(clientFrame, "<html>"
                        + "The Netplay Server is not accepting spectators at this time."
                        + "<br/><br/>Would you like to be player " + player
                        + " instead?</html>",
                        "Select Player");
            } else {
                dialog = new YesNoDialog(clientFrame, "<html>"
                        + "Player " + (prefs.getPlayer() + 1)
                        + " is not available.<br/><br/>"
                        + "Would you like to be a player " + player + " instead?</html>",
                        "Select Player");
            }
        } else {
            addActivity("User asked to be a spectator.");
            dialog = new YesNoDialog(clientFrame, "<html>"
                    + "The Netplay Server is not accepting remote players at this time."
                    + "<br/><br/>Would you like to be a spectator instead?</html>",
                    "Select Player");
        }
        window = dialog;
        dialog.setVisible(true);
        synchronized (stateMonitor) {
            if (state == DISCONNECTED) {
                window = null;
                return;
            }
        }
        if (dialog.isYes()) {
            addActivity("User accepted change of player.");
            prefs.setPlayer(playerIndex);
            AppPrefs.save();
            if (clientFrame != null) {
                clientFrame.loadFields();
            }
            setState(REQUEST_PLAYER);
        } else {
            addActivity("User declined change of player.");
            kill();
        }
        window = null;
    }

    private void choosePlayer() {
        EDT.async(() -> {
            final NetplayClientFrame clientFrame = getClientFrame();
            if (playerResponse == 0x00) {
                displayAccessDenied(clientFrame);
            } else {
                final int playerIndex = getSinglePlayerIndex();
                if (playerIndex >= 0) {
                    displaySinglePlayerPrompt(clientFrame, playerIndex);
                } else {
                    displayMultiplePlayersPrompt(clientFrame);
                }
            }
        });
    }

    private void displayPasswordPrompt(final boolean authenticationRequired) {
        EDT.async(() -> {
            final String title = authenticationRequired ? "Authentication Required"
                    : "Invalid Password";
            addActivity("User prompted for password.");
            final NetplayClientFrame clientFrame = getClientFrame();
            final PasswordDialog dialog = new PasswordDialog(clientFrame,
                    (authenticationRequired
                            ? "A password is required to log into the Netplay Server."
                            : "The specified password is incorrect.") +
                            "<br/><br/>Enter password to log into Netplay Server:", title);
            dialog.setPasswordRequired();
            dialog.setRememberPassword(prefs.isRememberPassword());
            dialog.setOkButtonText("Login");
            dialog.setOkButtonMnemonic('L');
            window = dialog;
            dialog.setVisible(true);
            synchronized (stateMonitor) {
                if (state == DISCONNECTED) {
                    window = null;
                    return;
                }
            }
            if (dialog.isOk()) {
                password = dialog.getPassword();
                prefs.setRememberPassword(dialog.isRememberPassword());
                if (dialog.isRememberPassword()) {
                    if (clientFrame != null) {
                        clientFrame.setPassword(password);
                    }
                    prefs.setPasswordLength(password.length);
                } else {
                    prefs.setPasswordLength(0);
                }
                AppPrefs.save();
                if (clientFrame != null) {
                    clientFrame.loadFields();
                }
                addActivity("User entered password.");
                setState(AUTHENTICATE);
            } else {
                addActivity("User canceled authentication.");
                kill();
            }
            window = null;
        });
    }

    private void setState(final ClientState state) {
        synchronized (stateMonitor) {
            this.state = state;
            stateMonitor.notifyAll();
        }
    }

    public String[] getQuickSaveNames() {
        return quickSaveNames;
    }

    public boolean isPlay() {
        return peekMessageType(Play, true);
    }

    public boolean isRewind() {
        return peekMessageType(Rewind, false);
    }

    private boolean peekMessageType(final int type,
                                    final boolean discardOnMatch) {

        if (!receiveQueue.consume(receiveTail, receiveElement)) {
            if (receiveQueue.isRunning()) {
                addActivity("Failed to peek message type.");
                addActivity("Breaking connection...");
                breakConnection();
            }
            return false;
        }

        if (receiveElement.messageType == type) {
            if (discardOnMatch) {
                receiveTail++;
            }
            return true;
        } else {
            return false;
        }
    }

    public int readMovieFrameIndex() {
        if (!(receiveQueue.consume(receiveTail++, receiveElement)
                && receiveElement.messageType == Rewind)) {
            if (receiveQueue.isRunning()) {
                addActivity("Failed to receive history index.");
                addActivity("Breaking connection...");
                breakConnection();
            }
            return 0;
        } else {
            return receiveElement.value;
        }
    }

    public void readControllerInput(final ControllerInput controllerInput) {

        if (!(receiveQueue.consume(receiveTail++, receiveElement)
                && receiveElement.messageType == ControllerInput)) {
            if (receiveQueue.isRunning()) {
                addActivity("Failed to receive controller input.");
                addActivity("Breaking connection...");
                controllerInput.input = 0;
                controllerInput.otherInputs = null;
                breakConnection();
            }
            return;
        }

        controllerInput.input = receiveElement.value;
        retryImmediately = true;
        if (receiveElement.data == null) {
            controllerInput.otherInputs = null;
        } else {
            try {
                controllerInput.otherInputs = (OtherInput[]) readObject(receiveElement
                        .data);
            } catch (final Throwable t) {
                addActivity("Controller input data corrupted.");
                addActivity("Breaking connection...");
                controllerInput.input = 0;
                controllerInput.otherInputs = null;
                breakConnection();
            }
        }
    }

    public void readFrameEnd() {
        if (!(receiveQueue.consume(receiveTail++, receiveElement)
                && receiveElement.messageType == FrameEnd)
                && receiveQueue.isRunning()) {
            addActivity("Failed to receive end of frame marker.");
            addActivity("Breaking connection...");
            breakConnection();
        }
    }

    public MovieBlock readMovieBlock() {
        if (!(receiveQueue.consume(receiveTail++, receiveElement)
                && receiveElement.messageType == MovieBlock)
                && receiveQueue.isRunning()) {
            addActivity("Failed to receive history data.");
            addActivity("Breaking connection...");
            breakConnection();
            return null;
        }

        try {
            return (MovieBlock) readObject(receiveElement.data);
        } catch (final Throwable t) {
            addActivity("History data corrupted.");
            addActivity("Breaking connection...");
            breakConnection();
            return null;
        }
    }

    public void writeControllerInput(final ControllerInput controllerInput) {
        if (running && !spectator) {
            sendQueue.produce(ControllerInput, controllerInput.input,
                    controllerInput.otherInputs);
        }
    }

    public void post(final int messageType) {
        if (running && !spectator) {
            sendQueue.produce(messageType);
        }
    }

    public void post(final int messageType, final int value) {
        if (running && !spectator) {
            sendQueue.produce(messageType, value);
        }
    }

    public void post(final int messageType, final Serializable value) {
        if (running && !spectator) {
            sendQueue.produce(messageType, value);
        }
    }

    private void send(final int messageType) throws Throwable {
        synchronized (writeMonitor) {
            out.write(messageType);
            out.flush();
        }
    }

    private void sendPasswordHash() throws Throwable {

        addActivity("Authenticating...");
        byte[] passwordHash;
        if (password == null) {
            passwordHash = prefs.getPasswordHash();
        } else {
            passwordHash = null;
            prefs.setPasswordHash(null);
            AppPrefs.save();
        }

        if (isBlank(salt)) {
            addActivity("Password not required.");
            setState(REQUEST_PLAYER);
            return;
        } else {
            if (isBlank(password)) {
                if (isBlank(passwordHash)) {
                    displayPasswordPrompt(true);
                    return;
                }
            } else if (isBlank(passwordHash)) {
                passwordHash = PasswordUtil.createHash(password, salt);
                if (prefs.isRememberPassword()) {
                    prefs.setPasswordHash(passwordHash);
                    prefs.setPasswordLength(password.length);
                    AppPrefs.save();
                }
            }
        }

        synchronized (writeMonitor) {
            out.write(PasswordHash);
            writeRawByteArray(out, passwordHash);
            out.flush();
        }
    }

    private void sendRequestFile() throws Throwable {
        addActivity("Requested file and save state.");
        send(FileRequest);
    }

    private void sendRequestPlayer() throws Throwable {
        addActivity("Requested player options.");
        synchronized (writeMonitor) {
            out.write(PlayerRequest);
            out.writeInt(prefs.getPlayer());
            out.flush();
        }
    }

    private void closeSocket() {
        try {
            final Socket s = socket;
            if (s != null) {
                socket = null;
                s.close();
            }
        } catch (final Throwable t) {
        }
    }

    private void breakConnection() {
        setState(DISCONNECTED);
        quickSaveNames = null;
        closeSocket();
        sendQueue.stop();
        receiveQueue.stop();
        interruptAll(heartbeatThread, sendThread, readMessageThread);
    }

    private void kill() {
        running = false;
        breakConnection();
        interrupt(mainThread);
    }

    public void stop() {
        kill();
        joinAll(mainThread, readMessageThread, sendThread, heartbeatThread);
        mainThread = null;
        readMessageThread = null;
        sendThread = null;
        heartbeatThread = null;
        quickSaveNames = null;
        App.dispose();
    }
}
