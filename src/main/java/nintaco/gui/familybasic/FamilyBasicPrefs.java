package nintaco.gui.familybasic;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class FamilyBasicPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer typePasteShortDelay;
    private Integer typePasteLongDelay;
    private Integer dataRecorderSamplingPeriod;

    public int getTypePasteShortDelay() {
        synchronized (AppPrefs.class) {
            if (typePasteShortDelay == null) {
                typePasteShortDelay = 1;
            }
            return typePasteShortDelay;
        }
    }

    public void setTypePasteShortDelay(final int typePasteshortDelay) {
        synchronized (AppPrefs.class) {
            this.typePasteShortDelay = typePasteshortDelay;
        }
    }

    public int getTypePasteLongDelay() {
        synchronized (AppPrefs.class) {
            if (typePasteLongDelay == null) {
                typePasteLongDelay = 20;
            }
            return typePasteLongDelay;
        }
    }

    public void setTypePasteLongDelay(final int typePasteLongDelay) {
        synchronized (AppPrefs.class) {
            this.typePasteLongDelay = typePasteLongDelay;
        }
    }

    public int getDataRecorderSamplingPeriod() {
        synchronized (AppPrefs.class) {
            if (dataRecorderSamplingPeriod == null) {
                dataRecorderSamplingPeriod = 88;
            }
            return dataRecorderSamplingPeriod;
        }
    }

    public void setDataRecorderSamplingPeriod(
            final int dataRecorderSamplingPeriod) {
        synchronized (AppPrefs.class) {
            this.dataRecorderSamplingPeriod = dataRecorderSamplingPeriod;
        }
    }
}