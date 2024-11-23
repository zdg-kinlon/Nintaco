package nintaco.gui.image;

import javax.swing.*;
import java.io.File;

public class QuickSaveStateInfo {

    private final int index;
    private final File file;
    private final long modifiedTime;
    private final JMenuItem loadMenuItem;
    private final JMenuItem saveMenuItem;

    public QuickSaveStateInfo(int index, File file, long modifiedTime,
                              JMenuItem loadMenuItem, JMenuItem saveMenuItem) {
        this.index = index;
        this.file = file;
        this.modifiedTime = modifiedTime;
        this.loadMenuItem = loadMenuItem;
        this.saveMenuItem = saveMenuItem;
    }

    public int getSlot() {
        return index;
    }

    public File getFile() {
        return file;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public JMenuItem getLoadMenuItem() {
        return loadMenuItem;
    }

    public JMenuItem getSaveMenuItem() {
        return saveMenuItem;
    }
}
