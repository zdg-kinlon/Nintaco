package nintaco.gui.mapmaker;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class MapMakerAppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String fileFormat;
    private Integer flushDelay;

    public int getFlushDelay() {
        synchronized (AppPrefs.class) {
            if (flushDelay == null) {
                flushDelay = 60;
            }
            return flushDelay;
        }
    }

    public void setFlushDelay(final int flushDelay) {
        synchronized (AppPrefs.class) {
            this.flushDelay = flushDelay;
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

    public void setFileFormat(final String fileFormat) {
        synchronized (AppPrefs.class) {
            this.fileFormat = fileFormat;
        }
    }
}
