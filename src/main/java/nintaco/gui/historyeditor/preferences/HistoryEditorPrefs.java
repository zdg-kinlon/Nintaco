package nintaco.gui.historyeditor.preferences;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class HistoryEditorPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Boolean trackCursor;
    private Boolean fastGeneration;
    private Boolean restorePosition;
    private boolean[] viewPlayers;
    private boolean[] recordPlayers;
    private Boolean merge;

    public boolean getTrackCursor() {
        synchronized (AppPrefs.class) {
            if (trackCursor == null) {
                trackCursor = true;
            }
            return trackCursor;
        }
    }

    public void setTrackCursor(final boolean trackCursor) {
        synchronized (AppPrefs.class) {
            this.trackCursor = trackCursor;
        }
    }

    public boolean getFastGeneration() {
        synchronized (AppPrefs.class) {
            if (fastGeneration == null) {
                fastGeneration = true;
            }
            return fastGeneration;
        }
    }

    public void setFastGeneration(final boolean fastGeneration) {
        synchronized (AppPrefs.class) {
            this.fastGeneration = fastGeneration;
        }
    }

    public boolean getRestorePosition() {
        synchronized (AppPrefs.class) {
            if (restorePosition == null) {
                restorePosition = true;
            }
            return restorePosition;
        }
    }

    public void setRestorePosition(Boolean restorePosition) {
        synchronized (AppPrefs.class) {
            this.restorePosition = restorePosition;
        }
    }

    public boolean[] getViewPlayers() {
        synchronized (AppPrefs.class) {
            if (viewPlayers == null) {
                viewPlayers = new boolean[]{true, true, false, false};
            }
            return viewPlayers;
        }
    }

    public void setViewPlayers(final boolean[] viewPlayers) {
        synchronized (AppPrefs.class) {
            this.viewPlayers = viewPlayers;
        }
    }

    public boolean[] getRecordPlayers() {
        synchronized (AppPrefs.class) {
            if (recordPlayers == null) {
                recordPlayers = new boolean[4];
            }
            return recordPlayers;
        }
    }

    public void setRecordPlayers(final boolean[] recordPlayers) {
        synchronized (AppPrefs.class) {
            this.recordPlayers = recordPlayers;
        }
    }

    public boolean getMerge() {
        synchronized (AppPrefs.class) {
            if (merge == null) {
                merge = false;
            }
            return merge;
        }
    }

    public void setMerge(final boolean merge) {
        synchronized (AppPrefs.class) {
            this.merge = merge;
        }
    }
}
