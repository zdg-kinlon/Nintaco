package nintaco.gui.nametables;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class NametablesPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer updateScanline;
    private Integer framesPerUpdate;
    private Integer scrollType;
    private Boolean updateOnSprite0Hit;
    private Boolean showTileGrid;
    private Boolean showAttributeGrid;

    public boolean isUpdateOnSprite0Hit() {
        synchronized (AppPrefs.class) {
            if (updateOnSprite0Hit == null) {
                updateOnSprite0Hit = false;
            }
            return updateOnSprite0Hit;
        }
    }

    public void setUpdateOnSprite0Hit(final boolean updateOnSprite0Hit) {
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

    public void setUpdateScanline(final int updateScanline) {
        synchronized (AppPrefs.class) {
            this.updateScanline = updateScanline;
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

    public void setFramesPerUpdate(final int framesPerUpdate) {
        synchronized (AppPrefs.class) {
            this.framesPerUpdate = framesPerUpdate;
        }
    }

    public Integer getScrollType() {
        synchronized (AppPrefs.class) {
            if (scrollType == null) {
                scrollType = NametablesFrame.ScrollType.Disabled;
            }
            return scrollType;
        }
    }

    public void setScrollType(final int scrollType) {
        synchronized (AppPrefs.class) {
            this.scrollType = scrollType;
        }
    }

    public boolean isShowTileGrid() {
        synchronized (AppPrefs.class) {
            if (showTileGrid == null) {
                showTileGrid = false;
            }
            return showTileGrid;
        }
    }

    public void setShowTileGrid(final boolean showTileGrid) {
        synchronized (AppPrefs.class) {
            this.showTileGrid = showTileGrid;
        }
    }

    public boolean isShowAttributeGrid() {
        synchronized (AppPrefs.class) {
            if (showAttributeGrid == null) {
                showAttributeGrid = false;
            }
            return showAttributeGrid;
        }
    }

    public void setShowAttributeGrid(final boolean showAttributeGrid) {
        synchronized (AppPrefs.class) {
            this.showAttributeGrid = showAttributeGrid;
        }
    }
}
