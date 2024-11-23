package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.BookmarksModel;
import nintaco.gui.historyeditor.HistoryBookmark;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public class BookmarkEditedChange
        extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    final HistoryBookmark priorBookmark;
    final HistoryBookmark newBookmark;

    public BookmarkEditedChange(final HistoryBookmark priorBookmark,
                                final HistoryBookmark newBookmark) {
        this.priorBookmark = priorBookmark;
        this.newBookmark = newBookmark;
        setDescription("Bookmark edited %d", newBookmark.getFrame());
    }

    @Override
    public int apply(final HistoryTableModel model) {
        final BookmarksModel bookmarksModel = model.getBookmarksModel();
        if (!priorBookmark.getName().equalsIgnoreCase(newBookmark.getName())) {
            bookmarksModel.delete(bookmarksModel.indexOf(priorBookmark));
        }
        bookmarksModel.add(newBookmark);
        model.setBookmarks(bookmarksModel.getBookmarkedRows());
        return -1;
    }

    @Override
    public int revert(final HistoryTableModel model) {
        final BookmarksModel bookmarksModel = model.getBookmarksModel();
        if (!priorBookmark.getName().equalsIgnoreCase(newBookmark.getName())) {
            bookmarksModel.delete(bookmarksModel.indexOf(newBookmark));
        }
        bookmarksModel.add(priorBookmark);
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
