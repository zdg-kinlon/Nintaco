package nintaco.gui.hexeditor.preferences;

import nintaco.preferences.GamePrefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HexEditorGamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private List<Bookmark> bookmarks;
    private String cpuMemoryFile;
    private String ppuMemoryFile;

    private void initBookmarks() {
        synchronized (GamePrefs.class) {
            if (bookmarks == null) {
                bookmarks = new ArrayList<>();
            }
        }
    }

    public String getCpuMemoryFile() {
        return cpuMemoryFile;
    }

    public void setCpuMemoryFile(String cpuMemoryFile) {
        synchronized (GamePrefs.class) {
            this.cpuMemoryFile = cpuMemoryFile;
        }
    }

    public String getPpuMemoryFile() {
        return ppuMemoryFile;
    }

    public void setPpuMemoryFile(String ppuMemoryFile) {
        synchronized (GamePrefs.class) {
            this.ppuMemoryFile = ppuMemoryFile;
        }
    }

    public List<Bookmark> getBookmarks() {
        initBookmarks();
        return bookmarks;
    }

    public boolean containsBookmark(int dataSourceIndex, int address) {
        synchronized (GamePrefs.class) {
            initBookmarks();
            for (final Bookmark bookmark : bookmarks) {
                if (bookmark.getAddress() == address
                        && bookmark.getDataSourceIndex() == dataSourceIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeBookmark(int dataSourceIndex, int address) {
        synchronized (GamePrefs.class) {
            initBookmarks();
            for (int i = bookmarks.size() - 1; i >= 0; i--) {
                final Bookmark bookmark = bookmarks.get(i);
                if (bookmark.getAddress() == address
                        && bookmark.getDataSourceIndex() == dataSourceIndex) {
                    bookmarks.remove(i);
                    break;
                }
            }
        }
    }

    public void addBookmark(int dataSourceIndex, int address, String name) {
        addBookmark(new Bookmark(dataSourceIndex, address, name));
    }

    public void addBookmark(Bookmark bookmark) {
        synchronized (GamePrefs.class) {
            initBookmarks();
            bookmarks.remove(bookmark);
            bookmarks.add(bookmark);
            while (bookmarks.size() > 32) {
                bookmarks.remove(0);
            }
        }
    }

    public void removeBookmark(Bookmark bookmark) {
        synchronized (GamePrefs.class) {
            initBookmarks();
            bookmarks.remove(bookmark);
        }
    }

    public void removeAllBookmarks() {
        synchronized (GamePrefs.class) {
            initBookmarks();
            bookmarks.clear();
        }
    }
}
