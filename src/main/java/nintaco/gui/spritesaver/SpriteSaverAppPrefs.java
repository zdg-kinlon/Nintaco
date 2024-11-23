package nintaco.gui.spritesaver;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class SpriteSaverAppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String fileFormat;
    private Integer imageScale;

    public int getImageScale() {
        synchronized (AppPrefs.class) {
            if (imageScale == null) {
                imageScale = 1;
            }
            return imageScale;
        }
    }

    public void setImageScale(int imageScale) {
        synchronized (AppPrefs.class) {
            this.imageScale = imageScale;
        }
    }

    public String getFileFormat() {
        synchronized (AppPrefs.class) {
            if (fileFormat == null) {
                return "png";
            }
            return fileFormat;
        }
    }

    public void setFileFormat(String fileFormat) {
        synchronized (AppPrefs.class) {
            this.fileFormat = fileFormat;
        }
    }
}
