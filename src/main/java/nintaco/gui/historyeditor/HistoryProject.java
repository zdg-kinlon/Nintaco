package nintaco.gui.historyeditor;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.change.HistoryChange;
import nintaco.gui.historyeditor.preferences.HistoryEditorPrefs;
import nintaco.movie.Movie;

import java.io.Serializable;
import java.util.List;

public class HistoryProject implements Serializable {

    private static final long serialVersionUID = 0;

    private String entryFileName;
    private int entryFileCRC;
    private HistoryEditorPrefs historyEditorPrefs;
    private Movie movie;
    private List<HistoryBookmark> bookmarks;
    private List<HistoryChange> changes;
    private int changesIndex;
    private int lastClickedRowIndex;
    private int headIndex;
    private int tailIndex;
    private int lastIndex;
    private IntPoint historyScrollValues;
    private IntPoint bookmarksScrollValues;
    private IntPoint changesScrollValues;

    public IntPoint getHistoryScrollValues() {
        return historyScrollValues;
    }

    public void setHistoryScrollValues(final IntPoint historyScrollValues) {
        this.historyScrollValues = historyScrollValues;
    }

    public IntPoint getBookmarksScrollValues() {
        return bookmarksScrollValues;
    }

    public void setBookmarksScrollValues(final IntPoint bookmarksScrollValues) {
        this.bookmarksScrollValues = bookmarksScrollValues;
    }

    public IntPoint getChangesScrollValues() {
        return changesScrollValues;
    }

    public void setChangesScrollValues(final IntPoint changesScrollValues) {
        this.changesScrollValues = changesScrollValues;
    }

    public int getHeadIndex() {
        return headIndex;
    }

    public void setHeadIndex(final int headIndex) {
        this.headIndex = headIndex;
    }

    public int getTailIndex() {
        return tailIndex;
    }

    public void setTailIndex(final int tailIndex) {
        this.tailIndex = tailIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(final int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public String getEntryFileName() {
        return entryFileName;
    }

    public void setEntryFileName(final String entryFileName) {
        this.entryFileName = entryFileName;
    }

    public int getEntryFileCRC() {
        return entryFileCRC;
    }

    public void setEntryFileCRC(final int entryFileCRC) {
        this.entryFileCRC = entryFileCRC;
    }

    public HistoryEditorPrefs getHistoryEditorPrefs() {
        return historyEditorPrefs;
    }

    public void setHistoryEditorPrefs(
            final HistoryEditorPrefs historyEditorPrefs) {
        this.historyEditorPrefs = historyEditorPrefs;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public List<HistoryBookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(final List<HistoryBookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<HistoryChange> getChanges() {
        return changes;
    }

    public void setChanges(final List<HistoryChange> changes) {
        this.changes = changes;
    }

    public int getChangesIndex() {
        return changesIndex;
    }

    public void setChangesIndex(final int changesIndex) {
        this.changesIndex = changesIndex;
    }

    public int getLastClickedRowIndex() {
        return lastClickedRowIndex;
    }

    public void setLastClickedRowIndex(final int lastClickedRowIndex) {
        this.lastClickedRowIndex = lastClickedRowIndex;
    }
}