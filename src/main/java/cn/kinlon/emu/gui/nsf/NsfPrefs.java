package cn.kinlon.emu.gui.nsf;

import cn.kinlon.emu.preferences.AppPrefs;

import java.io.Serializable;

public class NsfPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Boolean playInBackground;
    private Boolean automaticallyAdvanceTrack;
    private Integer silenceSeconds;
    private Boolean defaultTrackLength;
    private Integer trackLengthMinutes;

    public boolean isPlayInBackground() {
        synchronized (AppPrefs.class) {
            if (playInBackground == null) {
                playInBackground = true;
            }
            return playInBackground;
        }
    }

    public void setPlayInBackground(final boolean playInBackground) {
        synchronized (AppPrefs.class) {
            this.playInBackground = playInBackground;
        }
    }

    public boolean isAutomaticallyAdvanceTrack() {
        synchronized (AppPrefs.class) {
            if (automaticallyAdvanceTrack == null) {
                automaticallyAdvanceTrack = true;
            }
            return automaticallyAdvanceTrack;
        }
    }

    public void setAutomaticallyAdvanceTrack(
            final boolean automaticallyAdvanceTrack) {
        synchronized (AppPrefs.class) {
            this.automaticallyAdvanceTrack = automaticallyAdvanceTrack;
        }
    }

    public int getSilenceSeconds() {
        synchronized (AppPrefs.class) {
            if (silenceSeconds == null || silenceSeconds < 1 || silenceSeconds > 99) {
                silenceSeconds = 3;
            }
            return silenceSeconds;
        }
    }

    public void setSilenceSeconds(final int silenceSeconds) {
        synchronized (AppPrefs.class) {
            this.silenceSeconds = silenceSeconds;
        }
    }

    public boolean isDefaultTrackLength() {
        synchronized (AppPrefs.class) {
            if (defaultTrackLength == null) {
                defaultTrackLength = true;
            }
            return defaultTrackLength;
        }
    }

    public void setDefaultTrackLength(final boolean defaultTrackLength) {
        synchronized (AppPrefs.class) {
            this.defaultTrackLength = defaultTrackLength;
        }
    }

    public int getTrackLengthMinutes() {
        synchronized (AppPrefs.class) {
            if (trackLengthMinutes == null || trackLengthMinutes < 1
                    || trackLengthMinutes > 99) {
                trackLengthMinutes = 2;
            }
            return trackLengthMinutes;
        }
    }

    public void setTrackLengthMinutes(final int trackLengthMinutes) {
        synchronized (AppPrefs.class) {
            this.trackLengthMinutes = trackLengthMinutes;
        }
    }
}
