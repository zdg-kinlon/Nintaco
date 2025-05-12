package cn.kinlon.emu.gui.hexeditor.preferences;

import cn.kinlon.emu.gui.hexeditor.SearchText;
import cn.kinlon.emu.preferences.AppPrefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Search implements Serializable {

    private static final long serialVersionUID = 0;

    private List<SearchText> recentFinds;
    private List<SearchText> recentReplaces;

    private void initRecentFinds() {
        synchronized (AppPrefs.class) {
            if (recentFinds == null) {
                recentFinds = new ArrayList<>();
            }
        }
    }

    private void initRecentReplaces() {
        synchronized (AppPrefs.class) {
            if (recentReplaces == null) {
                recentReplaces = new ArrayList<>();
            }
        }
    }

    private void addRecent(final List<SearchText> list,
                           final SearchText value) {
        synchronized (AppPrefs.class) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(value)) {
                    list.remove(i);
                    break;
                }
            }
            while (list.size() > 9) {
                list.remove(list.size() - 1);
            }
            list.add(0, value);
        }
    }

}
