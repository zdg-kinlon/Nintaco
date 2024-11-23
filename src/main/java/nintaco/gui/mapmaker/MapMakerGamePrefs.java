package nintaco.gui.mapmaker;

import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;

import java.io.Serializable;

import static nintaco.gui.mapmaker.CaptureType.VisibleWindow;

public class MapMakerGamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String filePrefix;
    private Integer captureType;
    private Integer trackingSize;
    private Integer maxDifferences;
    private Integer updateScanline;
    private Integer startTileRow;
    private Integer endTileRow;
    private Boolean autoFlush;
    private Boolean autoPause;
    private Boolean updateOnSprite0Hit;

    public int getCaptureType() {
        synchronized (GamePrefs.class) {
            if (captureType == null) {
                captureType = VisibleWindow;
            }
            return captureType;
        }
    }

    public void setCaptureType(final int captureType) {
        synchronized (GamePrefs.class) {
            this.captureType = captureType;
        }
    }

    public int getTrackingSize() {
        synchronized (GamePrefs.class) {
            if (trackingSize == null) {
                trackingSize = 2;
            }
            return trackingSize;
        }
    }

    public void setTrackingSize(final int trackingSize) {
        synchronized (GamePrefs.class) {
            this.trackingSize = trackingSize;
        }
    }

    public int getMaxDifferences() {
        synchronized (GamePrefs.class) {
            if (maxDifferences == null) {
                return 160;
            }
            return maxDifferences;
        }
    }

    public void setMaxDifferences(final int maxDifferences) {
        synchronized (GamePrefs.class) {
            this.maxDifferences = maxDifferences;
        }
    }

    public boolean isAutoPause() {
        synchronized (GamePrefs.class) {
            if (autoPause == null) {
                autoPause = true;
            }
            return autoPause;
        }
    }

    public void setAutoPause(final boolean autoPause) {
        synchronized (GamePrefs.class) {
            this.autoPause = autoPause;
        }
    }

    public boolean isAutoFlush() {
        synchronized (GamePrefs.class) {
            if (autoFlush == null) {
                autoFlush = true;
            }
            return autoFlush;
        }
    }

    public void setAutoFlush(final boolean autoFlush) {
        synchronized (GamePrefs.class) {
            this.autoFlush = autoFlush;
        }
    }

    public String getFilePrefix() {
        synchronized (GamePrefs.class) {
            return filePrefix;
        }
    }

    public void setFilePrefix(final String filePrefix) {
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

    public void setUpdateOnSprite0Hit(final boolean updateOnSprite0Hit) {
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

    public void setUpdateScanline(final int updateScanline) {
        synchronized (AppPrefs.class) {
            this.updateScanline = updateScanline;
        }
    }

    public int getStartTileRow() {
        synchronized (AppPrefs.class) {
            if (startTileRow == null) {
                startTileRow = 0;
            }
            return startTileRow;
        }
    }

    public void setStartTileRow(final int startTileRow) {
        synchronized (AppPrefs.class) {
            this.startTileRow = startTileRow;
        }
    }

    public int getEndTileRow() {
        synchronized (AppPrefs.class) {
            if (endTileRow == null) {
                endTileRow = 29;
            }
            return endTileRow;
        }
    }

    public void setEndTileRow(final int endTileRow) {
        synchronized (AppPrefs.class) {
            this.endTileRow = endTileRow;
        }
    }
}