package nintaco.gui.netplay.client;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

import static nintaco.input.InputDevices.Gamepad1;

public class NetplayClientPrefs implements Serializable {

    public static final int DEFAULT_PORT = 8888;
    public static final int DEFAULT_PLAYER = 1; // port #2
    public static final int DEFAULT_INPUT_DEVICE = Gamepad1;
    private static final long serialVersionUID = 0;
    private String host;
    private Integer port;
    private byte[] passwordHash;
    private int passwordLength;
    private Boolean rememberPassword;
    private Integer player;
    private Integer inputDevice;

    public NetplayClientPrefs() {
    }

    public NetplayClientPrefs(final NetplayClientPrefs prefs) {
        this.host = prefs.host;
        this.port = prefs.port;
        this.passwordHash = prefs.passwordHash;
        this.passwordLength = prefs.passwordLength;
        this.rememberPassword = prefs.rememberPassword;
        this.player = prefs.player;
        this.inputDevice = prefs.inputDevice;
    }

    public String getHost() {
        synchronized (AppPrefs.class) {
            if (host == null) {
                host = "";
            }
            return host;
        }
    }

    public void setHost(final String host) {
        synchronized (AppPrefs.class) {
            this.host = host;
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

    public boolean isRememberPassword() {
        synchronized (AppPrefs.class) {
            if (rememberPassword == null) {
                rememberPassword = true;
            }
            return rememberPassword;
        }
    }

    public void setRememberPassword(final boolean rememberPassword) {
        synchronized (AppPrefs.class) {
            this.rememberPassword = rememberPassword;
        }
    }

    public int getPlayer() {
        synchronized (AppPrefs.class) {
            if (player == null) {
                player = DEFAULT_PLAYER;
            }
            return player;
        }
    }

    public void setPlayer(final int player) {
        synchronized (AppPrefs.class) {
            this.player = player;
        }
    }

    public int getInputDevice() {
        synchronized (AppPrefs.class) {
            if (inputDevice == null) {
                inputDevice = DEFAULT_INPUT_DEVICE;
            }
            return inputDevice;
        }
    }

    public void setInputDevice(final int inputDevice) {
        synchronized (AppPrefs.class) {
            this.inputDevice = inputDevice;
        }
    }
}