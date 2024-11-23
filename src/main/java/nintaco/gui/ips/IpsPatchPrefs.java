package nintaco.gui.ips;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class IpsPatchPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private String original;
    private String modified;
    private String patch;

    public String getOriginal() {
        synchronized (AppPrefs.class) {
            if (original == null) {
                original = "";
            }
            return original;
        }
    }

    public void setOriginal(final String original) {
        synchronized (AppPrefs.class) {
            this.original = original;
        }
    }

    public String getModified() {
        synchronized (AppPrefs.class) {
            if (modified == null) {
                modified = "";
            }
            return modified;
        }
    }

    public void setModified(final String modified) {
        synchronized (AppPrefs.class) {
            this.modified = modified;
        }
    }

    public String getPatch() {
        synchronized (AppPrefs.class) {
            if (patch == null) {
                patch = "";
            }
            return patch;
        }
    }

    public void setPatch(final String patch) {
        synchronized (AppPrefs.class) {
            this.patch = patch;
        }
    }
}