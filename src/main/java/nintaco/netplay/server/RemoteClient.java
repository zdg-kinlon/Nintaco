package nintaco.netplay.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

import nintaco.*;
import nintaco.files.*;
import nintaco.gui.netplay.server.*;
import nintaco.input.*;
import nintaco.movie.*;
import nintaco.netplay.protocol.*;
import nintaco.netplay.queue.*;
import nintaco.preferences.*;

import static nintaco.netplay.protocol.MessageType.*;
import static nintaco.netplay.protocol.Protocol.*;
import static nintaco.util.CollectionsUtil.*;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.ThreadUtil.*;

import nintaco.util.VersionUtil;

public class RemoteClient implements Runnable {

    private static final long MAX_PAUSE_TIME = TimeUnit.SECONDS.toMillis(20);
    private static final long HEARTBEAT_MILLIS = TimeUnit.SECONDS.toMillis(1);
    private static final long PAUSE_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private final ControllerInput receivedControllerInput = new ControllerInput();
    private final Object writeMonitor = new Object();
    private final Object pauseMonitor = new Object();
    private final RingQueue sendQueue = new RingQueue();
    private final NetplayServer server;
    private final Socket socket;
    private final NetplayServerPrefs prefs;

    private String remoteAddress;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private long pauseTime;

    private volatile Thread heartbeatThread;
    private volatile Thread sendThread;
    private volatile Thread mainThread;
    private volatile Thread pauseThread;
    private volatile int rewindTimeValue;
    private volatile int highSpeedValue;
    private volatile boolean running;
    private volatile boolean postEnabled;

    public RemoteClient(final NetplayServer server, final Socket socket,
                        final NetplayServerPrefs prefs) {
        this.server = server;
        this.socket = socket;
        this.prefs = prefs;
    }

    @Override
    public void run() {
        try {
            mainThread = Thread.currentThread();
            remoteAddress = String.format("[%s]", socket.getInetAddress()
                    .getHostAddress());
            server.addActivity("%s Connected.", remoteAddress);

            in = new DataInputStream(new BufferedInputStream(
                    socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(
                    socket.getOutputStream()));

            running = true;
            heartbeatThread = new Thread(this::heartbeatLoop,
                    "Netplay Server Heartbeat Thread");
            heartbeatThread.start();
            pauseThread = new Thread(this::pauseLoop,
                    "Netplay Server Pause Thread");
            pauseThread.start();
            sendThread = new Thread(this::sendLoop,
                    "Netplay Server Send Thread");
            sendThread.start();

            sendServerDescription();
            readMessageLoop();

        } catch (final Throwable t) {
//      t.printStackTrace();
        } finally {
            dispose();
        }

        server.removeRemoteClient(this);
        if (remoteAddress != null) {
            server.addActivity("%s Disconnected.", remoteAddress);
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void readMessageLoop() throws Throwable {
        while (running) {
            handleMessage(in.readUnsignedByte());
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
                    server.addActivity("%s Send underflow. Breaking connection...",
                            remoteAddress);
                    break;
                }
            }
        } catch (final Throwable t) {
//      t.printStackTrace();
            server.addActivity("%s Send transmission error. Breaking connection...",
                    remoteAddress);
        } finally {
            kill();
        }
    }

    private void heartbeatLoop() {
        try {
            while (running) {
                sendHeartbeat();
                sleep(HEARTBEAT_MILLIS);
            }
        } catch (final Throwable t) {
            kill();
        }
    }

    private void pauseLoop() {
        while (running) {
            final long time;
            synchronized (pauseMonitor) {
                time = pauseTime;
            }
            if (time != 0 && System.currentTimeMillis() - time > MAX_PAUSE_TIME) {
                server.addActivity("%s Timeout: failed to acknowledge file. "
                        + "Breaking connection...", remoteAddress);
                kill();
                break;
            }
            sleep(PAUSE_MILLIS);
        }
    }

    private void handleMessage(final int messageType) throws Throwable {
        switch (messageType) {
            case Heartbeat:
                break;
            case PasswordHash:
                handlePasswordHash();
                break;
            default:
                if (prefs.isEnablePassword() && !authenticated) {
                    kill();
                    return;
                }
                switch (messageType) {
                    case PlayerRequest:
                        handlePlayerRequest();
                        break;
                    case FileRequest:
                        handleFileRequest();
                        break;
                    case FileReceived:
                        handleFileReceived();
                        break;
                    case ControllerInput:
                        handleControllerInput();
                        break;
                    case Rewind:
                        handleRewindTime();
                        break;
                    case QuickSave:
                        handleQuickSave();
                        break;
                    case QuickLoad:
                        handleQuickLoad();
                        break;
                    case HighSpeed:
                        handleHighSpeed();
                        break;
                    default:
                        server.addActivity("%s Received unknown message type: %d.",
                                remoteAddress, messageType);
                        kill();
                        break;
                }
                break;
        }
    }

    public int readRewindTimeValue() {
        return rewindTimeValue;
    }

    public int readHighSpeedValue() {
        return highSpeedValue;
    }

    public ControllerInput readControllerInput() {
        return receivedControllerInput;
    }

    private void handleControllerInput() throws Throwable {
        receivedControllerInput.input = in.readInt();

        final OtherInput[] otherInputs = (OtherInput[]) readObject(
                readRawByteArray(in));
        if (otherInputs != null) {
            receivedControllerInput.otherInputs = otherInputs;
        }
    }

    private void handleRewindTime() throws Throwable {
        rewindTimeValue = in.readInt();
    }

    private void handleHighSpeed() throws Throwable {
        highSpeedValue = in.readInt();
    }

    private void handleQuickLoad() throws Throwable {
        final int slot = in.readInt();
        if (slot == 0) {
            server.addActivity("%s Quick loaded from newest slot.", remoteAddress);
        } else {
            server.addActivity("%s Quick loaded from slot %d.", remoteAddress, slot);
        }
        App.getImageFrame().quickLoadState(slot);
    }

    private void handleQuickSave() throws Throwable {
        final int slot = in.readInt();
        if (slot == 0) {
            server.addActivity("%s Quick saved to oldest slot.", remoteAddress);
        } else {
            server.addActivity("%s Quick saved to slot %d.", remoteAddress, slot);
        }
        App.getImageFrame().quickSaveState(slot);
    }

    private void handleFileReceived() throws Throwable {
        final int fileRequestID = in.readInt();
        server.addActivity("%s Acknowledged file transfer.", remoteAddress);
        synchronized (writeMonitor) {
            postEnabled = true;
            if (server.resume(this, fileRequestID)) {
                resetPauseTime();
            } else {
                postEnabled = false;
            }
        }
    }

    private void handlePasswordHash() throws Throwable {
        final byte[] passwordHash = readRawByteArray(in);

        server.addActivity("%s Authenticating...", remoteAddress);
        if (prefs.isEnablePassword()) {
            if (!compareArrays(prefs.getPasswordHash(), passwordHash)) {
                server.addActivity("%s Password invalid.", remoteAddress);
                sendWrongPasswordError();
                return;
            }
        } else {
            server.addActivity("%s Password not enabled.", remoteAddress);
        }

        authenticated = true;
        server.addActivity("%s Authenticated.", remoteAddress);
        sendAuthenticated();
    }

    private void handlePlayerRequest() throws Throwable {
        final int player = in.readInt();
        if (player < 4) {
            server.addActivity("%s Requests to be player %d.", remoteAddress,
                    player + 1);
        } else {
            server.addActivity("%s Requests to be a spectator.", remoteAddress);
        }
        final int playerResponse = server.requestPlayer(this, player);
        if (playerResponse == PlayerGranted) {
            server.addActivity("%s Request granted.", remoteAddress);
        } else {
            server.addActivity("%s Request denied.", remoteAddress);
        }
        synchronized (writeMonitor) {
            out.write(PlayerResponse);
            out.write(playerResponse);
            out.flush();
        }
    }

    private void resetPauseTime() {
        setPauseTime(0);
    }

    private void setPauseTime(final long pauseTime) {
        synchronized (pauseMonitor) {
            this.pauseTime = pauseTime;
            pauseMonitor.notifyAll();
        }
    }

    public void pause(final int fileRequestID) {
        server.pause(this, fileRequestID);
        setPauseTime(System.currentTimeMillis());
    }

    private void handleFileRequest() throws Throwable {
        server.addActivity("%s Requests file and saved state.", remoteAddress);
        final IFile file = App.getFile();
        final MachineRunner machineRunner = App.getMachineRunner();
        if (file != null && machineRunner != null) {
            final Movie movie = machineRunner.getMovie();
            synchronized (writeMonitor) {
                final Ports ports = AppPrefs.getInstance().getInputs().getPorts();
                final int fileRequestID = server.createFileRequestID();
                pause(fileRequestID);
                postFileAndSaveState(toByteArrayOutputStream(new FileResponse(
                        fileRequestID,
                        file,
                        machineRunner.getMachine(),
                        movie != null ? movie.getFrameIndex() : 0,
                        machineRunner.isForwardTime(),
                        machineRunner.getCurrentMovieBlock(),
                        machineRunner.getMovieBlock(),
                        ports.getConsoleType(),
                        ports.isMultitap(),
                        server.getQuickSaveStateMenuNames(),
                        App.getImageFrame().getFileInfo())).toByteArray(), true);
            }
        } else {
            postFileAndSaveState(null, false);
        }
    }

    public void postFileAndSaveState(final byte[] fileResponse,
                                     final boolean paused) {
        synchronized (writeMonitor) {
            if (paused) {
                postEnabled = false;
            }
            if (fileResponse != null) {
                sendQueue.produce(FileResponse, fileResponse);
                server.addActivity("%s Sent file data and saved state.", remoteAddress);
            } else {
                sendQueue.produce(NoFileResponse);
                server.addActivity("%s Sent file closed message.", remoteAddress);
            }
        }
    }

    public void post(final int messageType, final int value, final byte[] data) {
        if (postEnabled) {
            sendQueue.produce(messageType, value, data);
        }
    }

    public void post(final int messageType, final byte[] data) {
        if (postEnabled) {
            sendQueue.produce(messageType, data);
        }
    }

    public void post(final int messageType, final int value) {
        if (postEnabled) {
            sendQueue.produce(messageType, value);
        }
    }

    public void post(final int type) {
        if (postEnabled) {
            sendQueue.produce(type);
        }
    }

    private void sendAuthenticated() throws Throwable {
        synchronized (writeMonitor) {
            out.write(Authenticated);
            out.flush();
        }
    }

    private void sendWrongPasswordError() throws Throwable {
        synchronized (writeMonitor) {
            out.write(WrongPasswordError);
            out.flush();
        }
    }

    private void sendHeartbeat() throws Throwable {
        synchronized (writeMonitor) {
            out.write(Heartbeat);
            out.flush();
        }
    }

    private void sendServerDescription() throws Throwable {
        synchronized (writeMonitor) {
            out.write(ServerDescription);
            out.writeUTF(VersionUtil.getVersion());
            writeRawByteArray(out, prefs.getPasswordSalt());
            out.writeBoolean(prefs.isAllowQuickSaves());
            out.flush();
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (final Throwable t) {
        }
    }

    private void breakConnection() {
        closeSocket();
        interruptAll(mainThread, sendThread, heartbeatThread, pauseThread);
    }

    private void kill() {
        postEnabled = running = false;
        breakConnection();
        sendQueue.stop();
    }

    public void dispose() {
        kill();
    }

    @Override
    public String toString() {
        return remoteAddress;
    }
}
