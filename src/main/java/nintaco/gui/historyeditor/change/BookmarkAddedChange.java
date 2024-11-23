package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.BookmarksModel;
import nintaco.gui.historyeditor.HistoryBookmark;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public class BookmarkAddedChange extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    final HistoryBookmark bookmark;

    public BookmarkAddedChange(final HistoryBookmark bookmark) {
        this.bookmark = bookmark;
        setDescription("Bookmark added %d", bookmark.getFrame());
    }

    @Override
    public int apply(final HistoryTableModel model) {
        final BookmarksModel bookmarksModel = model.getBookmarksModel();
        bookmarksModel.add(bookmark);
        model.setBookmarks(bookmarksModel.getBookmarkedRows());
        return -1;
    }

    @Override
    public int revert(final HistoryTableModel model) {
        final BookmarksModel bookmarksModel = model.getBookmarksModel();
        bookmarksModel.delete(bookmarksModel.indexOf(bookmark));
        model.setBookmarks(bookmarksModel.getBookmarkedRows());
        return -1;
    }

    @Override
    public Map<IntPoint, Color> heat(final HistoryTableModel model,
                                     final int[] columnIndices, final Map<IntPoint, Color> hotCells,
                                     final Color color) {
        return hotCells;
    }
}
