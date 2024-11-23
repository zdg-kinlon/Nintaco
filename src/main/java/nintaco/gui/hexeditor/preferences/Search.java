package nintaco.gui.hexeditor.preferences;

import nintaco.gui.hexeditor.SearchText;
import nintaco.preferences.AppPrefs;

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

    public void addRecentFind(final SearchText value) {
        initRecentFinds();
        addRecent(recentFinds, value);
    }

    public void addRecentReplace(final SearchText value) {
        initRecentReplaces();
        addRecent(recentReplaces, value);
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

    public List<SearchText> getRecentReplaces() {
        initRecentReplaces();
        return recentReplaces;
    }

    public List<SearchText> getRecentFinds() {
        initRecentFinds();
        return recentFinds;
    }

    public SearchText getMostRecentFind() {
        synchronized (AppPrefs.class) {
            initRecentFinds();
            if (recentFinds.isEmpty()) {
                return null;
            } else {
                return recentFinds.get(0);
            }
        }
    }

    public SearchText getMostRecentReplace() {
        synchronized (AppPrefs.class) {
            initRecentReplaces();
            if (recentReplaces.isEmpty()) {
                return null;
            } else {
                return recentReplaces.get(0);
            }
        }
    }
}
