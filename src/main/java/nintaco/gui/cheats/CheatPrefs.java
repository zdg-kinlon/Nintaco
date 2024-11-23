package nintaco.gui.cheats;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class CheatPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private int exportFileType;
    private boolean showHex;

    public int getExportFileType() {
        return exportFileType;
    }

    public void setExportFileType(int exportFileType) {
        synchronized (AppPrefs.class) {
            this.exportFileType = exportFileType;
        }
    }

    public boolean isShowHex() {
        return showHex;
    }

    public void setShowHex(boolean showHex) {
        synchronized (AppPrefs.class) {
            this.showHex = showHex;
        }
    }
}
