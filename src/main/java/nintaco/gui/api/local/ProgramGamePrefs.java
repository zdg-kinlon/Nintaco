package nintaco.gui.api.local;

import nintaco.preferences.GamePrefs;

import java.io.Serializable;

public class ProgramGamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String jar;
    private String mainClass;
    private String arguments;

    public String getJar() {
        return jar;
    }

    public void setJar(final String jar) {
        synchronized (GamePrefs.class) {
            this.jar = jar;
        }
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(final String mainClass) {
        synchronized (GamePrefs.class) {
            this.mainClass = mainClass;
        }
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(final String arguments) {
        synchronized (GamePrefs.class) {
            this.arguments = arguments;
        }
    }
}
