package nintaco.gui.historyeditor;

import java.io.Serializable;

public class HistoryBookmark
        implements Serializable, Comparable<HistoryBookmark> {

    private static final long serialVersionUID = 0L;

    private String name;
    private int frame;

    public HistoryBookmark() {
    }

    public HistoryBookmark(final String name, final int frame) {
        this.name = name;
        this.frame = frame;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(final int frame) {
        this.frame = frame;
    }

    @Override
    public boolean equals(final Object obj) {
        final HistoryBookmark bookmark = (HistoryBookmark) obj;
        return name.equalsIgnoreCase(bookmark.name);
    }

    @Override
    public int compareTo(final HistoryBookmark bookmark) {
        return name.compareToIgnoreCase(bookmark.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
