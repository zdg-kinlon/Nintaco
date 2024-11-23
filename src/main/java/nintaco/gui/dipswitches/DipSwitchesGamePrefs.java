package nintaco.gui.dipswitches;

import nintaco.preferences.GamePrefs;

import java.io.Serializable;

public class DipSwitchesGamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private int[] dipSwitchValues;

    public int[] getDipSwitchValues() {
        synchronized (GamePrefs.class) {
            if (dipSwitchValues == null) {
                dipSwitchValues = new int[0];
            }
            return dipSwitchValues;
        }
    }

    public void setDipSwitchValues(final int[] dipSwitchValues) {
        synchronized (GamePrefs.class) {
            this.dipSwitchValues = dipSwitchValues;
        }
    }
}
