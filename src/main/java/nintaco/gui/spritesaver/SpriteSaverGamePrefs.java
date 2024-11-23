package nintaco.gui.spritesaver;

import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;

import java.io.Serializable;

public class SpriteSaverGamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String filePrefix;
    private Integer updateScanline;
    private Integer minOccurrences;
    private Integer withinSeconds;
    private Integer edgeMargin;
    private Boolean updateOnSprite0Hit;

    public int getEdgeMargin() {
        synchronized (AppPrefs.class) {
            if (edgeMargin == null) {
                edgeMargin = 16;
            }
            return edgeMargin;
        }
    }

    public void setEdgeMargin(int edgeMargin) {
        synchronized (AppPrefs.class) {
            this.edgeMargin = edgeMargin;
        }
    }

    public int getWithinSeconds() {
        synchronized (AppPrefs.class) {
            if (withinSeconds == null) {
                withinSeconds = 10;
            }
            return withinSeconds;
        }
    }

    public void setWithinSeconds(int withinSeconds) {
        synchronized (AppPrefs.class) {
            this.withinSeconds = withinSeconds;
        }
    }

    public int getMinOccurrences() {
        synchronized (AppPrefs.class) {
            if (minOccurrences == null) {
                minOccurrences = 3;
            }
            return minOccurrences;
        }
    }

    public void setMinOccurrences(int minOccurrences) {
        synchronized (AppPrefs.class) {
            this.minOccurrences = minOccurrences;
        }
    }

    public String getFilePrefix() {
        synchronized (GamePrefs.class) {
            return filePrefix;
        }
    }

    public void setFilePrefix(String filePrefix) {
        synchronized (GamePrefs.class) {
            this.filePrefix = filePrefix;
        }
    }

    public boolean isUpdateOnSprite0Hit() {
        synchronized (AppPrefs.class) {
            if (updateOnSprite0Hit == null) {
                updateOnSprite0Hit = false;
            }
            return updateOnSprite0Hit;
        }
    }

    public void setUpdateOnSprite0Hit(boolean updateOnSprite0Hit) {
        synchronized (AppPrefs.class) {
            this.updateOnSprite0Hit = updateOnSprite0Hit;
        }
    }

    public int getUpdateScanline() {
        synchronized (AppPrefs.class) {
            if (updateScanline == null) {
                updateScanline = 239;
            }
            return updateScanline;
        }
    }

    public void setUpdateScanline(int updateScanline) {
        synchronized (AppPrefs.class) {
            this.updateScanline = updateScanline;
        }
    }
}
