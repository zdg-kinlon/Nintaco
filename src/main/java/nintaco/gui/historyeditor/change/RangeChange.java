package nintaco.gui.historyeditor.change;

import nintaco.gui.historyeditor.BookmarksModel;
import nintaco.gui.historyeditor.HistoryBookmark;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.io.Serializable;

public abstract class RangeChange
        extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    protected final int rowIndex;
    protected final int[] buttons;

    protected HistoryBookmark[] deletedBookmarks;

    public RangeChange(final int rowIndex, final int[] buttons) {
        this.rowIndex = rowIndex;
        this.buttons = buttons;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getRowCount() {
        return buttons.length;
    }

    public int[] getButtons() {
        return buttons;
    }

    public int insert(final HistoryTableModel model) {
        if (buttons.length > 0) {
            final BookmarksModel bookmarksModel = model.getBookmarksModel();
            model.insertRows(rowIndex, buttons);
            bookmarksModel.handleFramesInserted(rowIndex,
                    rowIndex + buttons.length - 1);
            if (deletedBookmarks != null && deletedBookmarks.length > 0) {
                for (int i = deletedBookmarks.length - 1; i >= 0; i--) {
                    bookmarksModel.add(deletedBookmarks[i]);
                }
                model.setBookmarks(bookmarksModel.getBookmarkedRows());
            }
        }
        return rowIndex;
    }

    public int delete(final HistoryTableModel model) {
        if (buttons.length > 0) {
            final BookmarksModel bookmarksModel = model.getBookmarksModel();
            model.deleteRows(rowIndex, buttons);
            deletedBookmarks = bookmarksModel.handleFramesDeleted(rowIndex,
                    rowIndex + buttons.length - 1);
            model.setBookmarks(bookmarksModel.getBookmarkedRows());
        }
        return rowIndex;
    }
}
