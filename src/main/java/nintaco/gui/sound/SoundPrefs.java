package nintaco.gui.sound;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class SoundPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String audioDevice;
    private Integer latencyMillis;

    public String getAudioDevice() {
        synchronized (AppPrefs.class) {
            return audioDevice;
        }
    }

    public void setAudioDevice(final String audioDevice) {
        synchronized (AppPrefs.class) {
            this.audioDevice = audioDevice;
        }
    }

    public int getLatencyMillis() {
        synchronized (AppPrefs.class) {
            if (latencyMillis == null) {
                latencyMillis = 50;
            }
            return latencyMillis;
        }
    }

    public void setLatencyMillis(final int latencyMillis) {
        synchronized (AppPrefs.class) {
            this.latencyMillis = latencyMillis;
        }
    }
}