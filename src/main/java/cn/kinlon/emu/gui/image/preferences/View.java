package cn.kinlon.emu.gui.image.preferences;

import cn.kinlon.emu.gui.image.filters.VideoFilterDescriptor;
import cn.kinlon.emu.preferences.AppPrefs;

import java.awt.*;
import java.io.Serializable;

public class View implements Serializable {

    private static final long serialVersionUID = 0;

    private String lookAndFeelClassName;
    private String themeClassName;
    private Float fontScale;
    private Dimension fileChooserSize;
    private Boolean backgroundEnabled;
    private Boolean spritesEnabled;
    private Boolean spriteBoxesEnabled;
    private Boolean showFPS;
    private Boolean showInputDevices;
    private Boolean showStatusMessages;
    private Boolean noSpriteLimit;
    private Boolean tvAspect;
    private Boolean smoothScaling;
    private Boolean uniformPixelScaling;
    private Boolean underscan;
    private VideoFilterDescriptor videoFilter;

    public String getLookAndFeelClassName() {
        synchronized (AppPrefs.class) {
            return lookAndFeelClassName;
        }
    }

    public void setLookAndFeelClassName(String lookAndFeelClassName) {
        synchronized (AppPrefs.class) {
            this.lookAndFeelClassName = lookAndFeelClassName;
        }
    }

    public String getThemeClassName() {
        synchronized (AppPrefs.class) {
            return themeClassName;
        }
    }

    public void setThemeClassName(String themeClassName) {
        synchronized (AppPrefs.class) {
            this.themeClassName = themeClassName;
        }
    }

    public float getFontScale() {
        synchronized (AppPrefs.class) {
            if (fontScale == null) {
                fontScale = 1f;
            }
            return fontScale;
        }
    }

    public void setFontScale(float fontScale) {
        synchronized (AppPrefs.class) {
            this.fontScale = fontScale;
            this.fileChooserSize = null; // Reset JFileChooser size on font scale
        }
    }

    public Dimension getFileChooserSize() {
        synchronized (AppPrefs.class) {
            return fileChooserSize;
        }
    }

    public void setFileChooserSize(final Dimension fileChooserSize) {
        synchronized (AppPrefs.class) {
            this.fileChooserSize = fileChooserSize;
        }
    }

    public boolean isBackgroundEnabled() {
        synchronized (AppPrefs.class) {
            if (backgroundEnabled == null) {
                backgroundEnabled = true;
            }
            return backgroundEnabled;
        }
    }

    public boolean isSpritesEnabled() {
        synchronized (AppPrefs.class) {
            if (spritesEnabled == null) {
                spritesEnabled = true;
            }
            return spritesEnabled;
        }
    }

    public boolean isSpriteBoxesEnabled() {
        synchronized (AppPrefs.class) {
            if (spriteBoxesEnabled == null) {
                spriteBoxesEnabled = false;
            }
            return spriteBoxesEnabled;
        }
    }

    public boolean isShowFPS() {
        synchronized (AppPrefs.class) {
            if (showFPS == null) {
                showFPS = false;
            }
            return showFPS;
        }
    }

    public boolean isShowInputDevices() {
        synchronized (AppPrefs.class) {
            if (showInputDevices == null) {
                showInputDevices = false;
            }
            return showInputDevices;
        }
    }

    public boolean isShowStatusMessages() {
        synchronized (AppPrefs.class) {
            if (showStatusMessages == null) {
                showStatusMessages = true;
            }
            return showStatusMessages;
        }
    }

    public boolean isNoSpriteLimit() {
        synchronized (AppPrefs.class) {
            if (noSpriteLimit == null) {
                noSpriteLimit = false;
            }
            return noSpriteLimit;
        }
    }

    public boolean isTvAspect() {
        synchronized (AppPrefs.class) {
            if (tvAspect == null) {
                tvAspect = false;
            }
            return tvAspect;
        }
    }

    public void setTvAspect(final boolean tvAspect) {
        this.tvAspect = tvAspect;
    }

    public boolean isSmoothScaling() {
        synchronized (AppPrefs.class) {
            if (smoothScaling == null) {
                smoothScaling = false;
            }
            return smoothScaling;
        }
    }

    public void setSmoothScaling(final boolean smoothScaling) {
        synchronized (AppPrefs.class) {
            this.smoothScaling = smoothScaling;
        }
    }

    public boolean isUniformPixelScaling() {
        synchronized (AppPrefs.class) {
            if (uniformPixelScaling == null) {
                uniformPixelScaling = false;
            }
            return uniformPixelScaling;
        }
    }

    public void setUniformPixelScaling(final boolean uniformPixelScaling) {
        synchronized (AppPrefs.class) {
            this.uniformPixelScaling = uniformPixelScaling;
        }
    }

    public boolean isUnderscan() {
        synchronized (AppPrefs.class) {
            if (underscan == null) {
                underscan = false;
            }
            return underscan;
        }
    }

    public void setUnderscan(final boolean underscan) {
        synchronized (AppPrefs.class) {
            this.underscan = underscan;
        }
    }

    public VideoFilterDescriptor getVideoFilter() {
        synchronized (AppPrefs.class) {
            if (videoFilter == null) {
                videoFilter = VideoFilterDescriptor.NoFilter;
            }
            return videoFilter;
        }
    }

    public void setVideoFilter(VideoFilterDescriptor videoFilter) {
        synchronized (AppPrefs.class) {
            this.videoFilter = videoFilter;
        }
    }
}