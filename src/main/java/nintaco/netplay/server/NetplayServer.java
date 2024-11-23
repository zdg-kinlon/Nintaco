package nintaco.netplay.server;

import nintaco.gui.netplay.server.NetplayServerPrefs;

import java.io.*;
import java.net.*;
import java.util.*;

import nintaco.*;
import nintaco.files.*;
import nintaco.gui.image.*;
import nintaco.gui.netplay.server.*;
import nintaco.input.*;
import nintaco.movie.*;
import nintaco.netplay.protocol.*;
import nintaco.preferences.*;

import static java.nio.charset.StandardCharsets.*;
import static nintaco.input.ConsoleType.*;
import static nintaco.netplay.protocol.MessageType.*;
import static nintaco.netplay.protocol.Protocol.*;
import static nintaco.util.BitUtil.*;
import static nintaco.util.CollectionsUtil.*;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.ThreadUtil.*;

public class NetplayServer {

    public static final int BAOS_SIZE = 0x40000;

    private static final String NAME = "[Server]";

    private final List<RemoteClient> remoteClients = Collections
            .synchronizedList(new ArrayList<>());
    private final List<FileRequest> fileRequests = Collections
            .synchronizedList(new ArrayList<>());
    private final RemoteClient[] activePlayers = new RemoteClient[4];
    private final Object FileRequestIDSequenceMonitor = new Object();
    private final QuickSaveListener quickSaveListener = this::onQuickSaveChanged;

    private NetplayServerPrefs prefs;
    private Thread mainThread;
    private ServerSocket serverSocket;
    private boolean fourPlayers;
    private boolean[] localPlayers;
    private boolean allowRewindTime;
    private boolean allowHighSpeed;
    private boolean wasTrackingHistory;
    private int fileRequestIDSequence;

    private volatile boolean running;
    private volatile RemoteClient[] allPlayers;
    private volatile String[] quickSaveStateMenuNames;

    public void start() {

        if (mainThread != null) {
            stop();
        }

        mainThread = new Thread(this::run, "Netplay Server Thread");
        mainThread.start();
    }

    public boolean isRunning() {
        return running;
    }

    public void setMachineRunner(final MachineRunner machineRunner) {
        if (running) {
            if (machineRunner != null) {
                addActivity("%s File loaded.", NAME);
                machineRunner.setServer(this);
            } else {
                addActivity("%s File closed.", NAME);
            }
            postFileAndSaveState();
        }
    }

    void pause(final RemoteClient remoteClient, final int fileRequestID) {
        synchronized (fileRequests) {
            for (final FileRequest fileRequest : fileRequests) {
                if (fileRequest.getRemoteClient() == remoteClient
                        && fileRequest.getFileRequestID() == fileRequestID) {
                    return;
                }
            }
            if (fileRequests.isEmpty()) {
                App.setNoStepPause(true);
            }
            fileRequests.add(new FileRequest(remoteClient, fileRequestID));
        }
    }

    boolean resume(final RemoteClient remoteClient, final int fileRequestID) {
        boolean allRemoved = true;
        synchronized (fileRequests) {
            for (int i = fileRequests.size() - 1; i >= 0; i--) {
                final FileRequest fileRequest = fileRequests.get(i);
                if (fileRequest.getRemoteClient() == remoteClient) {
                    if (fileRequest.getFileRequestID() == fileRequestID) {
                        fileRequests.remove(i);
                    } else {
                        allRemoved = false;
                    }
                }
            }
            if (fileRequests.isEmpty()) {
                App.setNoStepPause(false);
            }
        }
        return allRemoved;
    }

    private void resume(final RemoteClient remoteClient) {
        synchronized (fileRequests) {
            for (int i = fileRequests.size() - 1; i >= 0; i--) {
                final FileRequest fileRequest = fileRequests.get(i);
                if (fileRequest.getRemoteClient() == remoteClient) {
                    fileRequests.remove(i);
                }
            }
            if (fileRequests.isEmpty()) {
                App.setNoStepPause(false);
            }
        }
    }

    public int mergeHighSpeed(int highSpeedValue) {
        if (allowHighSpeed) {
            for (int i = 3; i >= 0; i--) {
                if (!localPlayers[i]) {
                    highSpeedValue &= ~(1 << i);
                    final RemoteClient remoteClient = activePlayers[i];
                    if (remoteClient != null) {
                        final int v = remoteClient.readHighSpeedValue();
                        highSpeedValue |= (v & (1 << i));
                    }
                }
            }
        } else {
            for (int i = 3; i >= 0; i--) {
                if (!localPlayers[i]) {
                    highSpeedValue &= ~(1 << i);
                }
            }
        }
        return highSpeedValue;
    }

    public int mergeRewindTime(int rewindTimeValue) {
        if (allowRewindTime) {
            for (int i = 3; i >= 0; i--) {
                if (!localPlayers[i]) {
                    rewindTimeValue &= ~(0x21 << i);
                    final RemoteClient remoteClient = activePlayers[i];
                    if (remoteClient != null) {
                        final int v = remoteClient.readRewindTimeValue();
                        rewindTimeValue |= (v & (1 << i)) | ((v & 0x10) << (i + 1));
                    }
                }
            }
        } else {
            for (int i = 3; i >= 0; i--) {
                if (!localPlayers[i]) {
                    rewindTimeValue &= ~(0x21 << i);
                }
            }
        }
        return rewindTimeValue;
    }

    public void mergeControllerInput(final ControllerInput controllerInput) {
        int mergedInput = 0;
        for (int i = 3; i >= 0; i--) {
            if (localPlayers[i]) {
                mergedInput |= controllerInput.input & (0xFF << (i << 3));
            } else {
                final RemoteClient remoteClient = activePlayers[i];
                if (remoteClient != null) {
                    final ControllerInput in = remoteClient.readControllerInput();
                    mergedInput |= in.input & (0xFF << (i << 3));
                    if (in.otherInputs != null) {
                        controllerInput.otherInputs = concat(OtherInput.class,
                                controllerInput.otherInputs, in.otherInputs);
                        in.otherInputs = null;
                    }
                }
            }
        }
        controllerInput.input = mergedInput;
    }

    public void postSaveState(final MachineRunner machineRunner) {
        if (running && machineRunner != null) {
            machineRunner.setServer(this);
            final byte[] saveState = toByteArrayOutputStream(machineRunner
                    .getMachine()).toByteArray();
            final RemoteClient[] clients = allPlayers;
            for (int i = clients.length - 1; i >= 0; i--) {
                final RemoteClient remoteClient = clients[i];
                if (remoteClient != null) {
                    remoteClient.post(SaveState, saveState);
                }
            }
        }
    }

    private void postFileAndSaveState() {
        if (running) {
            final IFile file = App.getFile();
            final MachineRunner machineRunner = App.getMachineRunner();
            byte[] fileResponse = null;

            final RemoteClient[] clients = allPlayers;
            if (file != null && machineRunner != null) {
                final Ports ports = AppPrefs.getInstance().getInputs().getPorts();
                final Movie movie = machineRunner.getMovie();
                final int fileRequestID = createFileRequestID();
                for (int i = clients.length - 1; i >= 0; i--) {
                    final RemoteClient remoteClient = clients[i];
                    if (remoteClient != null) {
                        remoteClient.pause(fileRequestID);
                    }
                }
                fileResponse = toByteArrayOutputStream(new FileResponse(
                        fileRequestID,
                        file,
                        machineRunner.getMachine(),
                        movie != null ? movie.getFrameIndex() : 0,
                        machineRunner.isForwardTime(),
                        machineRunner.getCurrentMovieBlock(),
                        machineRunner.getMovieBlock(),
                        ports.getConsoleType(),
                        ports.isMultitap(),
                        quickSaveStateMenuNames,
                        App.getImageFrame().getFileInfo())).toByteArray();
            }

            for (int i = clients.length - 1; i >= 0; i--) {
                final RemoteClient remoteClient = clients[i];
                if (remoteClient != null) {
                    remoteClient.postFileAndSaveState(fileResponse, false);
                }
            }
        }
    }

    public void writeControllerInput(final ControllerInput controllerInput) {
        if (running) {
            post(ControllerInput, controllerInput.input, controllerInput.otherInputs);
        }
    }

    public void post(final int messageType, final String str) {
        if (running) {
            post(messageType, str == null ? null : str.getBytes(ISO_8859_1));
        }
    }

    public void post(final int messageType, final Serializable serializable) {
        if (running) {
            post(messageType, serializable == null ? null
                    : toByteArrayOutputStream(serializable).toByteArray());
        }
    }

    public void post(final int messageType, final int value,
                     final Serializable serializable) {
        if (running) {
            post(messageType, value, serializable == null ? null
                    : toByteArrayOutputStream(serializable).toByteArray());
        }
    }

    public void post(final int messageType, final int value, final byte[] data) {
        if (running) {
            final RemoteClient[] clients = allPlayers;
            for (int i = clients.length - 1; i >= 0; i--) {
                final RemoteClient remoteClient = clients[i];
                if (remoteClient != null) {
                    remoteClient.post(messageType, value, data);
                }
            }
        }
    }

    public void post(final int messageType, final byte[] data) {
        if (running) {
            final RemoteClient[] clients = allPlayers;
            for (int i = clients.length - 1; i >= 0; i--) {
                final RemoteClient remoteClient = clients[i];
                if (remoteClient != null) {
                    remoteClient.post(messageType, data);
                }
            }
        }
    }

    public void post(final int messageType, final int value) {
        if (running) {
            final RemoteClient[] clients = allPlayers;
            for (int i = clients.length - 1; i >= 0; i--) {
                final RemoteClient remoteClient = clients[i];
                if (remoteClient != null) {
                    remoteClient.post(messageType, value);
                }
            }
        }
    }

    public void post(final int type) {
        if (running) {
            final RemoteClient[] clients = allPlayers;
            for (int i = clients.length - 1; i >= 0; i--) {
                final RemoteClient remoteClient = clients[i];
                if (remoteClient != null) {
                    remoteClient.post(type);
                }
            }
        }
    }

    int createFileRequestID() {
        synchronized (FileRequestIDSequenceMonitor) {
            return fileRequestIDSequence++;
        }
    }

    int requestPlayer(final RemoteClient remoteClient, final int player) {
        synchronized (remoteClients) {
            removePlayer(remoteClient);
            int bits = prefs.isAllowSpectators() ? 1 : 0;
            for (int i = activePlayers.length - 1; i >= 0; i--) {
                bits = (bits << 1) | ((!localPlayers[i] && activePlayers[i] == null
                        && (i < 2 || fourPlayers)) ? 1 : 0);
            }
            if (!remoteClient.isRunning()) {
                return bits;
            }
            if (getBitBool(bits, player)) {
                if (player < 4) {
                    activePlayers[player] = remoteClient;
                }
                outer:
                {
                    for (int i = allPlayers.length - 1; i >= 0; i--) {
                        if (remoteClient == allPlayers[i]) {
                            break outer;
                        }
                    }
                    final RemoteClient[] clients
                            = new RemoteClient[allPlayers.length + 1];
                    System.arraycopy(allPlayers, 0, clients, 0, allPlayers.length);
                    clients[allPlayers.length] = remoteClient;
                    allPlayers = clients;
                }
                return PlayerGranted;
            } else {
                return bits;
            }
        }
    }

    private void removePlayer(final RemoteClient remoteClient) {
        resume(remoteClient);
        synchronized (remoteClients) {
            for (int i = activePlayers.length - 1; i >= 0; i--) {
                if (activePlayers[i] == remoteClient) {
                    activePlayers[i] = null;
                }
            }
            for (int i = allPlayers.length - 1; i >= 0; i--) {
                if (allPlayers[i] == remoteClient) {
                    final RemoteClient[] clients
                            = new RemoteClient[allPlayers.length - 1];
                    for (int j = allPlayers.length - 1, k = clients.length - 1; j >= 0;
                         j--) {
                        if (j != i) {
                            clients[k--] = allPlayers[j];
                        }
                    }
                    allPlayers = clients;
                }
            }
        }
    }

    void removeRemoteClient(final RemoteClient remoteClient) {
        synchronized (remoteClients) {
            removePlayer(remoteClient);
            remoteClients.remove(remoteClient);
            remoteClients.notifyAll();
        }
    }

    void addActivity(final String activity, final Object... params) {
        final NetplayServerFrame serverFrame = App.getNetworkServerFrame();
        if (serverFrame != null) {
            serverFrame.addActivity(activity, params);
        }
    }

    private void setServerStatus(final boolean serverUp) {
        final NetplayServerFrame serverFrame = App.getNetworkServerFrame();
        if (serverFrame != null) {
            serverFrame.setServerStatus(serverUp);
        }
    }

    String[] getQuickSaveStateMenuNames() {
        return quickSaveStateMenuNames;
    }

    private void run() {

        final AppPrefs appPrefs = AppPrefs.getInstance();
        final Ports ports = appPrefs.getInputs().getPorts();

        wasTrackingHistory = appPrefs.getHistoryPrefs().isTrackHistory();
        if (!wasTrackingHistory) {
            appPrefs.getHistoryPrefs().setTrackHistory(true);
            App.setTrackHistory(true);
        }

        prefs = appPrefs.getNetplayServerPrefs();
        allowRewindTime = prefs.isAllowRewindTime();
        allowHighSpeed = prefs.isAllowHighSpeed();
        localPlayers = prefs.getLocalPlayers();
        fileRequestIDSequence = 0;
        fourPlayers = ports.isMultitap() || ports.getConsoleType() == VsDualSystem;
        for (int i = activePlayers.length - 1; i >= 0; i--) {
            activePlayers[i] = null;
        }
        allPlayers = new RemoteClient[0];
        remoteClients.clear();
        fileRequests.clear();

        try {
            serverSocket = new ServerSocket(prefs.getPort(), 50,
                    prefs.getLocalIPAddress());
        } catch (final Throwable t) {
            addActivity("%s Failed to start: %s", NAME, t.getMessage());
            setServerStatus(false);
            return;
        }

        addActivity("%s Started.", NAME);
        addActivity("%s Listening for clients on port %d.", NAME, prefs.getPort());
        setServerStatus(true);

        try {
            running = true;
            final MachineRunner machineRunner = App.getMachineRunner();
            if (machineRunner != null) {
                machineRunner.setServer(this);
            }
            App.getImageFrame().addQuickSaveListener(quickSaveListener);
            while (running) {
                final RemoteClient remoteClient = new RemoteClient(this,
                        serverSocket.accept(), prefs);
                remoteClients.add(remoteClient);
                new Thread(remoteClient).start();
            }
        } catch (final Throwable t) {
            stop();
        } finally {
            App.getImageFrame().removeQuickSaveListener(quickSaveListener);
            quickSaveStateMenuNames = null;
        }

        synchronized (remoteClients) {
            while (!remoteClients.isEmpty()) {
                threadWait(remoteClients);
            }
        }

        final MachineRunner machineRunner = App.getMachineRunner();
        if (machineRunner != null) {
            machineRunner.clearServer();
        }
        if (!wasTrackingHistory) {
            appPrefs.getHistoryPrefs().setTrackHistory(false);
            App.setTrackHistory(false);
        }
        addActivity("%s Stopped.", NAME);
        setServerStatus(false);
    }

    private void onQuickSaveChanged(
            final List<QuickSaveStateInfo> quickSaveStateInfos) {

        if (running) {
            final String[] menuNames = new String[quickSaveStateInfos.size()];
            for (int i = quickSaveStateInfos.size() - 1; i >= 0; i--) {
                menuNames[i] = quickSaveStateInfos.get(i).getLoadMenuItem().getText();
            }
            quickSaveStateMenuNames = menuNames;
            post(QuickSaveStateMenuNames, quickSaveStateMenuNames);
        }
    }

    public void stop() {
        running = false;
        synchronized (remoteClients) {
            for (final RemoteClient remoteClient : remoteClients) {
                remoteClient.dispose();
            }
        }
        try {
            final ServerSocket s = serverSocket;
            if (s != null) {
                serverSocket = null;
                s.close();
            }
        } catch (final Throwable t) {
        }
        final Thread t = mainThread;
        if (t != null) {
            mainThread = null;
            interrupt(t);
            join(t);
        }
    }
}
