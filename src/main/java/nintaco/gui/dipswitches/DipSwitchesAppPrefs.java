package nintaco.gui.dipswitches;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class DipSwitchesAppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Boolean displayDialogOnLoad;
    private Boolean resetMachine;

    public boolean isDisplayDialogOnLoad() {
        synchronized (AppPrefs.class) {
            if (displayDialogOnLoad == null) {
                displayDialogOnLoad = true;
            }
            return displayDialogOnLoad;
        }
    }

    public void setDisplayDialogOnLoad(final boolean displayDialogOnLoad) {
        synchronized (AppPrefs.class) {
            this.displayDialogOnLoad = displayDialogOnLoad;
        }
    }

    public boolean isResetMachine() {
        synchronized (AppPrefs.class) {
            if (resetMachine == null) {
                resetMachine = true;
            }
            return resetMachine;
        }
    }

    public void setResetMachine(final boolean resetMachine) {
        synchronized (AppPrefs.class) {
            this.resetMachine = resetMachine;
        }
    }
}