package nintaco.gui.hexeditor.preferences;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HexEditorAppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Search search;
    private List<Integer> recentAddresses;

    private int fadeFrames;

    private void initFadeFrames() {
        synchronized (AppPrefs.class) {
            if (fadeFrames == 0) {
                fadeFrames = 16;
            }
        }
    }

    private void initRecentAddresses() {
        synchronized (AppPrefs.class) {
            if (recentAddresses == null) {
                recentAddresses = new ArrayList<>();
            }
        }
    }

    public List<Integer> getRecentAddresses() {
        initRecentAddresses();
        return recentAddresses;
    }

    public void addRecentAddress(int address) {
        synchronized (AppPrefs.class) {
            initRecentAddresses();
            recentAddresses.remove((Integer) address);
            recentAddresses.add(0, address);
            while (recentAddresses.size() > 10) {
                recentAddresses.remove(recentAddresses.size() - 1);
            }
        }
    }

    public int getFadeFrames() {
        initFadeFrames();
        return fadeFrames;
    }

    public void setFadeFrames(int fadeFrames) {
        synchronized (AppPrefs.class) {
            if (fadeFrames <= 0) {
                fadeFrames = 16;
            } else if (fadeFrames > 250) {
                fadeFrames = 250;
            }
            this.fadeFrames = fadeFrames;
        }
    }

    public Search getSearch() {
        synchronized (AppPrefs.class) {
            if (search == null) {
                search = new Search();
            }
            return search;
        }
    }
}
