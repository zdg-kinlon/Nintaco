package cn.kinlon.emu.gui.hexeditor.preferences;

import cn.kinlon.emu.preferences.AppPrefs;

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

    public int getFadeFrames() {
        initFadeFrames();
        return fadeFrames;
    }

}
