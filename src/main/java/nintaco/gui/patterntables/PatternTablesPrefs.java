package nintaco.gui.patterntables;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class PatternTablesPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer updateScanline;
    private Integer colorSetIndex;
    private Integer framesPerUpdate;
    private Boolean updateOnSprite0Hit;

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
                updateScanline = 96;
            }
            return updateScanline;
        }
    }

    public void setUpdateScanline(int updateScanline) {
        synchronized (AppPrefs.class) {
            this.updateScanline = updateScanline;
        }
    }

    public int getColorSetIndex() {
        synchronized (AppPrefs.class) {
            if (colorSetIndex == null) {
                colorSetIndex = -1;
            }
            return colorSetIndex;
        }
    }

    public void setColorSetIndex(int colorSetIndex) {
        synchronized (AppPrefs.class) {
            this.colorSetIndex = colorSetIndex;
        }
    }

    public int getFramesPerUpdate() {
        synchronized (AppPrefs.class) {
            if (framesPerUpdate == null) {
                framesPerUpdate = 1;
            }
            return framesPerUpdate;
        }
    }

    public void setFramesPerUpdate(int framesPerUpdate) {
        synchronized (AppPrefs.class) {
            this.framesPerUpdate = framesPerUpdate;
        }
    }
}
