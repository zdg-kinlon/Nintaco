package nintaco.gui.oam;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class OamDataPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer updateScanline;
    private Integer framesPerUpdate;
    private Integer spriteIndex;
    private Boolean updateOnSprite0Hit;
    private Boolean colorSprites;
    private Boolean showHiddenSprites;
    private Boolean highlightSelectedSprite;

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

    public int getSpriteIndex() {
        synchronized (AppPrefs.class) {
            if (spriteIndex == null) {
                spriteIndex = -1;
            }
            return spriteIndex;
        }
    }

    public void setSpriteIndex(final int spriteIndex) {
        synchronized (AppPrefs.class) {
            this.spriteIndex = spriteIndex;
        }
    }

    public boolean isColorSprites() {
        synchronized (AppPrefs.class) {
            if (colorSprites == null) {
                colorSprites = true;
            }
            return colorSprites;
        }
    }

    public void setColorSprites(final boolean colorSprites) {
        synchronized (AppPrefs.class) {
            this.colorSprites = colorSprites;
        }
    }

    public boolean isShowHiddenSprites() {
        synchronized (AppPrefs.class) {
            if (showHiddenSprites == null) {
                showHiddenSprites = false;
            }
            return showHiddenSprites;
        }
    }

    public void setShowHiddenSprites(final boolean showHiddenSprites) {
        synchronized (AppPrefs.class) {
            this.showHiddenSprites = showHiddenSprites;
        }
    }

    public boolean isHighlightSelectedSprite() {
        synchronized (AppPrefs.class) {
            if (highlightSelectedSprite == null) {
                highlightSelectedSprite = false;
            }
            return highlightSelectedSprite;
        }
    }

    public void setHighlightSelectedSprite(
            final boolean highlightSelectedSprite) {
        synchronized (AppPrefs.class) {
            this.highlightSelectedSprite = highlightSelectedSprite;
        }
    }
}