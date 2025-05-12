package cn.kinlon.emu.gui.image.preferences;

import cn.kinlon.emu.preferences.AppPrefs;

import java.io.Serializable;

public class HistoryPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Boolean trackHistory;

    public boolean isTrackHistory() {
        synchronized (AppPrefs.class) {
            if (trackHistory == null) {
                trackHistory = true;
            }
            return trackHistory;
        }
    }

}
