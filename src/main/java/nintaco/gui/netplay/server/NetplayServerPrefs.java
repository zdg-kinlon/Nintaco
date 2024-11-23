package nintaco.gui.netplay.server;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;
import java.net.InetAddress;

public class NetplayServerPrefs implements Serializable {

    public static final int DEFAULT_PORT = 8888;
    private static final long serialVersionUID = 0;
    private InetAddress localIPAddress;
    private Integer port;
    private boolean[] localPlayers;
    private Boolean enablePassword;
    private byte[] passwordSalt;
    private byte[] passwordHash;
    private int passwordLength;
    private Boolean allowSpectators;
    private Boolean allowRewindTime;
    private Boolean allowHighSpeed;
    private Boolean allowQuickSaves;

    public NetplayServerPrefs() {
    }

    public NetplayServerPrefs(final NetplayServerPrefs prefs) {
        this.localIPAddress = prefs.localIPAddress;
        this.port = prefs.port;
        this.localPlayers = new boolean[4];
        System.arraycopy(prefs.localPlayers, 0, this.localPlayers, 0,
                this.localPlayers.length);
        this.enablePassword = prefs.enablePassword;
        this.passwordSalt = prefs.passwordSalt;
        this.passwordHash = prefs.passwordHash;
        this.passwordLength = prefs.passwordLength;
        this.allowSpectators = prefs.allowSpectators;
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

    public boolean[] getLocalPlayers() {
        synchronized (AppPrefs.class) {
            if (localPlayers == null) {
                localPlayers = new boolean[4];
            }
            return localPlayers;
        }
    }

    public void setLocalPlayers(final boolean[] localPlayers) {
        synchronized (AppPrefs.class) {
            this.localPlayers = localPlayers;
        }
    }

    public boolean isEnablePassword() {
        synchronized (AppPrefs.class) {
            if (enablePassword == null) {
                enablePassword = false;
            }
            return enablePassword;
        }
    }

    public void setEnablePassword(final boolean enablePassword) {
        synchronized (AppPrefs.class) {
            this.enablePassword = enablePassword;
        }
    }

    public boolean isAllowRewindTime() {
        synchronized (AppPrefs.class) {
            if (allowRewindTime == null) {
                allowRewindTime = true;
            }
            return allowRewindTime;
        }
    }

    public void setAllowRewindTime(final boolean allowRewindTime) {
        synchronized (AppPrefs.class) {
            this.allowRewindTime = allowRewindTime;
        }
    }

    public boolean isAllowHighSpeed() {
        synchronized (AppPrefs.class) {
            if (allowHighSpeed == null) {
                allowHighSpeed = true;
            }
            return allowHighSpeed;
        }
    }

    public void setAllowHighSpeed(final boolean allowHighSpeed) {
        synchronized (AppPrefs.class) {
            this.allowHighSpeed = allowHighSpeed;
        }
    }

    public boolean isAllowQuickSaves() {
        synchronized (AppPrefs.class) {
            if (allowQuickSaves == null) {
                allowQuickSaves = true;
            }
            return allowQuickSaves;
        }
    }

    public void setAllowQuickSaves(final boolean allowQuickSaves) {
        synchronized (AppPrefs.class) {
            this.allowQuickSaves = allowQuickSaves;
        }
    }

    public boolean isAllowSpectators() {
        synchronized (AppPrefs.class) {
            if (allowSpectators == null) {
                allowSpectators = true;
            }
            return allowSpectators;
        }
    }

    public void setAllowSpectators(final boolean allowSpectators) {
        synchronized (AppPrefs.class) {
            this.allowSpectators = allowSpectators;
        }
    }

    public byte[] getPasswordSalt() {
        synchronized (AppPrefs.class) {
            return passwordSalt;
        }
    }

    public void setPasswordSalt(final byte[] passwordSalt) {
        synchronized (AppPrefs.class) {
            this.passwordSalt = passwordSalt;
        }
    }

    public byte[] getPasswordHash() {
        synchronized (AppPrefs.class) {
            return passwordHash;
        }
    }

    public void setPasswordHash(final byte[] passwordHash) {
        synchronized (AppPrefs.class) {
            this.passwordHash = passwordHash;
        }
    }

    public int getPasswordLength() {
        synchronized (AppPrefs.class) {
            return passwordLength;
        }
    }

    public void setPasswordLength(final int passwordLength) {
        synchronized (AppPrefs.class) {
            this.passwordLength = passwordLength;
        }
    }
}