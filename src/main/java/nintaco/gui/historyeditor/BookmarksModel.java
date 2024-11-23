package nintaco.gui.historyeditor;

import nintaco.gui.Int;

import javax.swing.*;
import java.util.*;

public class BookmarksModel extends AbstractListModel<HistoryBookmark> {

    private final List<HistoryBookmark> bookmarks = new ArrayList<>();

    public List<HistoryBookmark> getBookmarks() {
        return new ArrayList(bookmarks);
    }

    public void setBookmarks(final List<HistoryBookmark> bookmarks) {
        clear();
        this.bookmarks.addAll(bookmarks);
        if (!this.bookmarks.isEmpty()) {
            fireIntervalAdded(this, 0, this.bookmarks.size() - 1);
        }
    }

    public void clear() {
        final int priorLastIndex = this.bookmarks.size() - 1;
        this.bookmarks.clear();
        if (priorLastIndex >= 0) {
            fireIntervalRemoved(this, 0, priorLastIndex);
        }
    }

    public void add(final HistoryBookmark bookmark) {

        final int index = bookmarks.indexOf(bookmark);
        if (index >= 0) {
            bookmarks.set(index, bookmark);
            fireContentsChanged(this, index, index);
        } else {
            bookmarks.add(bookmark);
            Collections.sort(bookmarks);
            fireContentsChanged(this, 0, bookmarks.size() - 1);
        }
    }

    public HistoryBookmark delete(final int index) {

        HistoryBookmark bookmark = null;
        if (index >= 0 && index < bookmarks.size()) {
            bookmark = bookmarks.remove(index);
            fireIntervalRemoved(this, index, index);
        }
        return bookmark;
    }

    public void handleFramesInserted(final int startIndex, final int endIndex) {
        final int insertedFrames = endIndex - startIndex + 1;
        for (int i = bookmarks.size() - 1; i >= 0; i--) {
            final HistoryBookmark bookmark = bookmarks.get(i);
            final int frame = bookmark.getFrame();
            if (frame >= startIndex) {
                bookmarks.set(i, new HistoryBookmark(bookmark.getName(),
                        frame + insertedFrames));
            }
        }
    }

    public HistoryBookmark[] handleFramesDeleted(final int startIndex,
                                                 final int endIndex) {
        final List<HistoryBookmark> deletedBookmarks = new ArrayList<>();
        final int deletedFrames = endIndex - startIndex + 1;
        for (int i = bookmarks.size() - 1; i >= 0; i--) {
            final HistoryBookmark bookmark = bookmarks.get(i);
            final int frame = bookmark.getFrame();
            if (frame >= startIndex && frame <= endIndex) {
                deletedBookmarks.add(bookmarks.remove(i));
                fireIntervalRemoved(this, i, i);
            } else if (frame > endIndex) {
                bookmarks.set(i, new HistoryBookmark(bookmark.getName(),
                        frame - deletedFrames));
            }
        }
        return deletedBookmarks.toArray(
                new HistoryBookmark[deletedBookmarks.size()]);
    }

    public HistoryBookmark findBookmark(final int frame) {
        for (int i = bookmarks.size() - 1; i >= 0; i--) {
            final HistoryBookmark bookmark = bookmarks.get(i);
            if (bookmark.getFrame() == frame) {
                return bookmark;
            }
        }
        return null;
    }

    public int indexOf(final HistoryBookmark bookmark) {
        return bookmarks.indexOf(bookmark);
    }

    public Set<Int> getBookmarkedRows() {
        final Set<Int> set = new HashSet<>();
        for (int i = bookmarks.size() - 1; i >= 0; i--) {
            set.add(new Int(bookmarks.get(i).getFrame()));
        }
        return set;
    }

    public boolean isEmpty() {
        return bookmarks.isEmpty();
    }

    @Override
    public int getSize() {
        return bookmarks.size();
    }

    @Override
    public HistoryBookmark getElementAt(final int index) {
        return bookmarks.get(index);
    }
}
