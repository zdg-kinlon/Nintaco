package nintaco.api.server;

import nintaco.App;
import nintaco.gui.api.server.ProgramServerFrame;
import nintaco.gui.api.server.ProgramServerPrefs;
import nintaco.preferences.AppPrefs;

import java.net.ServerSocket;
import java.net.Socket;

import static nintaco.util.ThreadUtil.*;

public class ProgramServer {

    private static final String NAME = "[Server]";
    private static final long DELAY_MILLIS = 1000L;

    private ProgramServerPrefs prefs;
    private Thread mainThread;
    private ServerSocket serverSocket;
    private LocalProxy proxy;

    private volatile boolean running;

    public boolean isRunning() {
        return running;
    }

    public void start() {

        if (mainThread != null) {
            stop();
        }

        mainThread = new Thread(this::run, "Program Server Thread");
        mainThread.start();
    }

    void addActivity(final String activity, final Object... params) {
        final ProgramServerFrame programFrame = App.getProgramServerFrame();
        if (programFrame != null) {
            programFrame.addActivity(activity, params);
        }
    }

    private void setServerStatus(final boolean serverUp) {
        final ProgramServerFrame programFrame = App.getProgramServerFrame();
        if (programFrame != null) {
            programFrame.setServerStatus(serverUp);
        }
    }

    private void run() {

        prefs = AppPrefs.getInstance().getProgramServerPrefs();

        try {
            serverSocket = new ServerSocket(prefs.getPort(), 50,
                    prefs.getLocalIPAddress());
        } catch (final Throwable t) {
            addActivity("%s Failed to start: %s", NAME, t.getMessage());
            setServerStatus(false);
            return;
        }

        addActivity("%s Started.", NAME);
        addActivity("%s Listening for client on port %d.", NAME, prefs.getPort());
        setServerStatus(true);

        try {
            running = true;
            while (running) {
                handleSocket(serverSocket.accept());
                sleep(DELAY_MILLIS);
            }
        } catch (final Throwable t) {
            stop();
        }

        addActivity("%s Stopped.", NAME);
        setServerStatus(false);
    }

    private void handleSocket(final Socket socket) {
        String remoteAddress = null;
        try {
            remoteAddress = String.format("[%s]", socket.getInetAddress()
                    .getHostAddress());
            addActivity("%s Connected.", remoteAddress);

            proxy = new LocalProxy();
            proxy.run(socket);

        } catch (final Throwable t) {
            //t.printStackTrace();
        } finally {
            if (proxy != null) {
                proxy.dispose();
                proxy = null;
            }
            try {
                socket.close();
            } catch (final Throwable t) {
            }
        }

        if (remoteAddress != null) {
            addActivity("%s Disconnected.", remoteAddress);
        }
    }

    public void stop() {
        running = false;
        final LocalProxy localProxy = proxy;
        if (localProxy != null) {
            localProxy.dispose();
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
