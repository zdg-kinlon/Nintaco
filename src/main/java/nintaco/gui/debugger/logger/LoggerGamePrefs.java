package nintaco.gui.debugger.logger;

import nintaco.preferences.GamePrefs;

import java.io.Serializable;

import static nintaco.files.FileUtil.createLogFile;

public class LoggerGamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String fileName;

    public String getFileName() {
        synchronized (GamePrefs.class) {
            if (fileName == null) {
                fileName = createLogFile();
            }
            return fileName;
        }
    }

    public void setFileName(String fileName) {
        synchronized (GamePrefs.class) {
            this.fileName = fileName;
        }
    }
}
