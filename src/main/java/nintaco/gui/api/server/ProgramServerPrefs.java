package nintaco.gui.api.server;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;
import java.net.InetAddress;

public class ProgramServerPrefs implements Serializable {

    public static final int DEFAULT_PORT = 9999;
    private static final long serialVersionUID = 0;
    private InetAddress localIPAddress;
    private Integer port;
    private Boolean runInBackground;

    public ProgramServerPrefs() {
    }

    public ProgramServerPrefs(final ProgramServerPrefs prefs) {
        this.localIPAddress = prefs.localIPAddress;
        this.port = prefs.port;
    }

    public InetAddress getLocalIPAddress() {
        synchronized (AppPrefs.class) {
            return localIPAddress;
        }
    }

    public void setLocalIPAddress(final InetAddress localIPAddress) {
        synchronized (AppPrefs.class) {
            this.localIPAddress = localIPAddress;
        }
    }

    public int getPort() {
        synchronized (AppPrefs.class) {
            if (port == null) {
                port = DEFAULT_PORT;
            }
            return port;
        }
    }

    public void setPort(final int port) {
        synchronized (AppPrefs.class) {
            this.port = port;
        }
    }

    public boolean isRunInBackground() {
        synchronized (AppPrefs.class) {
            if (runInBackground == null) {
                runInBackground = true;
            }
            return runInBackground;
        }
    }

    public void setRunInBackground(final boolean runInBackground) {
        synchronized (AppPrefs.class) {
            this.runInBackground = runInBackground;
        }
    }
}