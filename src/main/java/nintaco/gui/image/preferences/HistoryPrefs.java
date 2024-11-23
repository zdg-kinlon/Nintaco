package nintaco.gui.image.preferences;

import nintaco.preferences.AppPrefs;

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

    public void setTrackHistory(final boolean trackHistory) {
        synchronized (AppPrefs.class) {
            this.trackHistory = trackHistory;
        }
    }
}
