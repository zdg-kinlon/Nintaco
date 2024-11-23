package nintaco.gui.historyeditor;

import nintaco.gui.Int;
import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.change.*;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static nintaco.movie.Movie.*;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.BitUtil.toggleBit;
import static nintaco.util.MathUtil.clamp;
import static nintaco.util.MathUtil.roundUpDivision;

public class HistoryTableModel extends AbstractTableModel {

    public static final String SQUARE = "25A0";
    public static final String TRIANGLE = "25BA";
    public static final String BLUE = "66CCFF";
    public static final String TAN = "A58A57";
    public static final String PINK = "FC9D9F";

    public static final String COLOR_SYMBOL = "<font color='#%s'>&#x%s;</font>";

    public static final String[] HTMLS = new String[8];

    public static final String[] BUTTON_NAMES
            = {"A", "B", "S", "T", "U", "D", "L", "R"};

    public static final int HOT_LENGTH = 16;
    public static final Color[] HOT_COLORS = new Color[HOT_LENGTH];

    static {
        for (int i = 0; i < HOT_LENGTH; i++) {
            double v = ((double) (i + 1)) / HOT_LENGTH;
            v = Math.sqrt(v);
            HOT_COLORS[i] = new Color((int) (255 * v), 0, 0);
        }
        for (int i = 0; i < 8; i++) {
            if (i == 0) {
                HTMLS[i] = "";
            } else {
                final StringBuilder sb = new StringBuilder("<html>");
                if (getBitBool(i, 2)) {
                    sb.append(String.format(COLOR_SYMBOL, PINK, SQUARE));
                }
                if (getBitBool(i, 1)) {
                    sb.append(String.format(COLOR_SYMBOL, TAN, TRIANGLE));
                }
                if (getBitBool(i, 0)) {
                    sb.append(String.format(COLOR_SYMBOL, BLUE, TRIANGLE));
                }
                sb.append("</html>");
                HTMLS[i] = sb.toString();
            }
        }
    }

    private final int[] playerIndices = new int[4];
    private final int[] columnIndices = new int[32];
    private final ChangeListModel changeListModel;
    private final BookmarksModel bookmarksModel;
    private final Int key = new Int();

    private Movie movie;
    private Set<Int> bookmarks = new HashSet<>();
    private Map<IntPoint, Color> hotCells = new HashMap<>();
    private boolean[] viewPlayers;
    private int columnCount = 10;
    private int players = 1;
    private int headIndex;
    private int lastIndex = -1;
    private int tailIndex;

    public HistoryTableModel(final ChangeListModel changeListModel,
                             final BookmarksModel bookmarksModel) {
        this.changeListModel = changeListModel;
        this.bookmarksModel = bookmarksModel;
        setViewPlayers(new boolean[]{true, true, false, false});
    }

    public static String createRange(final String name, int start, int end) {

        start = max(0, start);
        end = max(0, end);

        if (start == end) {
            return String.format("%s %d", name, start);
        } else {
            return String.format("%s %d\u2013%d", name, start, end);
        }
    }

    public BookmarksModel getBookmarksModel() {
        return bookmarksModel;
    }

    public ChangeListModel getChangeListModel() {
        return changeListModel;
    }

    public int undo() {
        if (changeListModel.getChangesIndex() > 1) {
            changeListModel.decrementChangesIndex();
            final int rowIndex = changeListModel.getElementAt(
                    changeListModel.getChangesIndex()).revert(this);
            updateHotCells();
            if (rowIndex >= 0 && rowIndex - 1 < tailIndex) {
                setTailIndex(rowIndex - 1);
            }
            return rowIndex;
        } else {
            return -1;
        }
    }

    public int redo() {
        if (changeListModel.getChangesIndex() < changeListModel.getSize()) {
            final int rowIndex = changeListModel.getElementAt(
                    changeListModel.getChangesIndex()).apply(this);
            changeListModel.incrementChangesIndex();
            updateHotCells();
            if (rowIndex >= 0 && rowIndex - 1 < tailIndex) {
                setTailIndex(rowIndex - 1);
            }
            return rowIndex;
        } else {
            return -1;
        }
    }

    public void insertButtons(final int rowIndex, final int rowCount) {
        insertButtons(rowIndex, new int[rowCount]);
    }

    public void insertButtons(final int rowIndex, final int[] buttons) {

        if (buttons == null || buttons.length == 0) {
            return;
        }

        addChange(rowIndex, new InsertChange(rowIndex, buttons),
                createRange("Insert", rowIndex, rowIndex + buttons.length - 1));
    }

    public void trimBottom(final int rowIndex) {

        if (rowIndex < 0) {
            return;
        }

        final int rowCount = getRowCount() - rowIndex;
        addChange(rowIndex, new DeleteChange(rowIndex, rowCount), createRange(
                "Trim bottom", rowIndex, rowIndex + rowCount - 1));
    }

    public DeleteChange deleteButtons(final int[] rows) {

        DeleteChange trimTopChange = null;
        if (rows.length == 0) {
            return trimTopChange;
        }
        Arrays.sort(rows);

        final List<DeleteChange> deleteChanges = new ArrayList<>();
        final int lastRowIndex = rows.length - 1;
        for (int i = 0, j = 0; i <= lastRowIndex; i++) {
            if (i == lastRowIndex || rows[i] + 1 != rows[i + 1]) {
                final DeleteChange change = new DeleteChange(rows[j], i - j + 1);
                if (change.getRowIndex() == 0) {
                    trimTopChange = change;
                } else {
                    deleteChanges.add(change);
                }
                j = i + 1;
            }
        }

        if (!deleteChanges.isEmpty()) {
            Collections.reverse(deleteChanges);
            addChange(rows[0], new Multichange<>(deleteChanges.toArray(
                    new DeleteChange[deleteChanges.size()])), createRange("Delete",
                    rows[0], rows[rows.length - 1]));
        }

        return trimTopChange;
    }

    public void deleteRows(final int rowIndex, final int[] buttons) {

        if (buttons.length == 0) {
            return;
        }

        final int rowCount = getRowCount();
        final List<MovieBlock> blocks = movie.getMovieBlocks();
        final int endIndex = rowCount - 1;
        final int newFrameCount = rowCount - buttons.length;
        final int newBlocksSize = max(1,
                roundUpDivision(newFrameCount, BLOCK_SIZE));

        int index1 = rowIndex;
        int blocksIndex1 = index1 >> BLOCK_SHIFT;
        int[] buttons1 = blocks.get(blocksIndex1).buttons;
        index1 &= BLOCK_MASK;
        for (int i = 0; ; i++) {
            buttons[i] = buttons1[index1];
            if (i == buttons.length - 1) {
                break;
            }
            if (++index1 == BLOCK_SIZE) {
                index1 = 0;
                buttons1 = blocks.get(++blocksIndex1).buttons;
            }
        }
        index1 = rowIndex;
        blocksIndex1 = index1 >> BLOCK_SHIFT;
        buttons1 = blocks.get(blocksIndex1).buttons;
        index1 &= BLOCK_MASK;

        int index2 = rowIndex + buttons.length;
        int row = index2;
        if (row <= endIndex) {
            int blocksIndex2 = index2 >> BLOCK_SHIFT;
            int[] buttons2 = blocks.get(blocksIndex2).buttons;
            index2 &= BLOCK_MASK;

            while (true) {
                buttons1[index1] = buttons2[index2];

                if (++row > endIndex) {
                    break;
                }

                if (++index1 == BLOCK_SIZE) {
                    index1 = 0;
                    buttons1 = blocks.get(++blocksIndex1).buttons;
                }
                if (++index2 == BLOCK_SIZE) {
                    index2 = 0;
                    buttons2 = blocks.get(++blocksIndex2).buttons;
                }
            }
        }

        movie.frameCount = newFrameCount;
        while (blocks.size() > newBlocksSize) {
            blocks.remove(blocks.size() - 1);
        }
        int i = movie.getFrameCount() - 1;
        final int[] bs = getMovieBlock(i).buttons;
        i = (i & BLOCK_MASK) + 1;
        while (i < BLOCK_SIZE) {
            bs[i++] = 0;
        }

        fireTableRowsDeleted(rowIndex, rowIndex + buttons.length - 1);

        if (headIndex >= getRowCount()) {
            setHeadIndex(getRowCount() - 1);
        }
        if (lastIndex >= getRowCount()) {
            setLastIndex(getRowCount() - 1);
        }
        if (tailIndex >= getRowCount()) {
            setTailIndex(getRowCount() - 1);
        }
    }

    public void clear() {
        clear(null);
    }

    public void clear(final byte[] saveState) {
        final int lastRowIndex = getRowCount() - 1;
        headIndex = 0;
        lastIndex = -1;
        tailIndex = 0;
        bookmarks.clear();
        bookmarksModel.clear();
        changeListModel.clear();
        hotCells.clear();
        movie.clear(saveState);
        fireTableRowsDeleted(0, max(0, lastRowIndex));
    }

    public void insertRows(int rowIndex, final int[] buttons) {

        if (buttons.length == 0) {
            return;
        }

        final int frameCount = getRowCount();
        final List<MovieBlock> blocks = movie.getMovieBlocks();
        final int newFrameCount = frameCount + buttons.length;
        final int newBlocksSize = max(1,
                roundUpDivision(newFrameCount, BLOCK_SIZE));
        while (blocks.size() < newBlocksSize) {
            blocks.add(new MovieBlock());
        }

        if (rowIndex < 0) {
            rowIndex = 0;
        } else if (rowIndex >= frameCount) {
            rowIndex = frameCount;
        }

        if (frameCount == 0 || rowIndex == frameCount) {

            int index1 = frameCount;
            int blocksIndex1 = index1 >> BLOCK_SHIFT;
            int[] buttons1 = blocks.get(blocksIndex1).buttons;
            index1 &= BLOCK_MASK;

            if (buttons.length > 0) {
                for (int i = 0; ; ) {
                    buttons1[index1] = buttons[i];
                    if (++i >= buttons.length) {
                        break;
                    }
                    if (++index1 >= BLOCK_SIZE) {
                        index1 = 0;
                        buttons1 = blocks.get(++blocksIndex1).buttons;
                    }
                }
            }

            movie.frameCount = newFrameCount;
            fireTableRowsInserted(rowIndex, rowIndex + buttons.length - 1);
            return;
        }

        int index2 = newFrameCount - 1;
        int index1 = index2 - buttons.length;
        int row = index1;

        int blocksIndex1 = index1 >> BLOCK_SHIFT;
        int blocksIndex2 = index2 >> BLOCK_SHIFT;

        int[] buttons1 = blocks.get(blocksIndex1).buttons;
        int[] buttons2 = blocks.get(blocksIndex2).buttons;

        index1 &= BLOCK_MASK;
        index2 &= BLOCK_MASK;

        while (true) {

            buttons2[index2] = buttons1[index1];

            if (row == rowIndex) {
                break;
            } else {
                row--;
            }

            if (--index1 < 0) {
                index1 = BLOCK_MASK;
                buttons1 = blocks.get(--blocksIndex1).buttons;
            }
            if (--index2 < 0) {
                index2 = BLOCK_MASK;
                buttons2 = blocks.get(--blocksIndex2).buttons;
            }
        }

        if (buttons.length > 0) {
            for (int i = 0; ; ) {
                buttons1[index1] = buttons[i];
                if (++i >= buttons.length) {
                    break;
                }
                if (++index1 >= BLOCK_SIZE) {
                    index1 = 0;
                    buttons1 = blocks.get(++blocksIndex1).buttons;
                }
            }
        }

        movie.frameCount = newFrameCount;
        fireTableRowsInserted(rowIndex, rowIndex + buttons.length - 1);
    }

    public void pasteButtons(final int rowIndex, final Integer[] buttons,
                             final boolean merge) {

        if (buttons == null || buttons.length == 0) {
            return;
        }

        final int rowCount = getRowCount();
        final List<ButtonsChange> buttonsChanges = new ArrayList<>();
        for (int i = buttons.length - 1; i >= 0; i--) {
            final Integer bs = buttons[i];
            final int row = rowIndex + i;
            if (bs != null && row < rowCount) {
                final int b = getButtons(row);
                buttonsChanges.add(new ButtonsChange(row, b, merge ? (b | bs) : bs));
            }
        }

        addChange(rowIndex, new Multichange<>(buttonsChanges), createRange("Paste",
                rowIndex, rowIndex + buttons.length - 1));
    }

    public void clearButtons(final int[] rowIndices) {

        if (rowIndices.length == 0) {
            return;
        }

        final ButtonsChange[] bcs = new ButtonsChange[rowIndices.length];
        int minRowIndex = Integer.MAX_VALUE;
        int maxRowIndex = Integer.MIN_VALUE;
        for (int i = rowIndices.length - 1; i >= 0; i--) {
            final int rowIndex = rowIndices[i];
            minRowIndex = min(rowIndex, minRowIndex);
            maxRowIndex = max(rowIndex, maxRowIndex);
            bcs[i] = new ButtonsChange(rowIndex, getButtons(rowIndex), 0);
        }

        addChange(minRowIndex, new Multichange<>(bcs), createRange("Clear",
                minRowIndex, maxRowIndex));
    }

    public void toggleButton(final int rowIndex, final int columnIndex) {

        final int[] buttons = getMovieBlock(rowIndex).buttons;
        final int blockIndex = rowIndex & BLOCK_MASK;
        final int col = columnIndex - 2;
        final int bit = playerIndices[col >> 3] | col;
        final int bs = buttons[blockIndex];

        addChange(rowIndex, new ButtonsChange(rowIndex, buttons[blockIndex],
                        toggleBit(bs, bit)), "%s %s %d", getBitBool(bs, bit) ? "Reset" : "Set",
                BUTTON_NAMES[col & 7], rowIndex);
    }

    public void handleEndRecord(int[] priorButtons,
                                final int priorRowCount, final int priorHeadIndex) {

        outer:
        {
            for (int i = priorButtons.length - 1; i >= 0; i--) {
                if (priorButtons[i] != getButtons(priorHeadIndex + i)) {
                    break outer;
                }
            }
            priorButtons = null;
        }

        InsertChange insertChange = null;
        final int rowCount = getRowCount();
        final int rowsAdded = rowCount - priorRowCount;
        if (rowsAdded > 0) {
            final int[] buttons = new int[rowsAdded];
            final int lastPriorRowIndex = priorRowCount - 1;
            for (int i = buttons.length - 1; i >= 0; i--) {
                buttons[i] = getButtons(lastPriorRowIndex + i);
            }
            insertChange = new InsertChange(priorRowCount, buttons);
        }

        if (!(priorButtons == null && insertChange == null)) {
            final RunChange runChange = new RunChange(priorHeadIndex, getHeadIndex(),
                    priorButtons, insertChange);
            changeListModel.addChange(runChange);
            updateHotCells();
        }
    }

    public void clearChanges() {
        changeListModel.clear();
        updateHotCells();
    }

    public void addChange(final HistoryChange change) {
        changeListModel.addChange(change);
        change.apply(this);
        updateHotCells();
    }

    public void addChange(final int rowIndex, final HistoryChange change,
                          final String description, final Object... params) {
        change.setDescription(description, params);
        addChange(change);
        if (rowIndex - 1 < tailIndex) {
            setTailIndex(rowIndex - 1);
        }
    }

    private void updateHotCells() {
        final Map<IntPoint, Color> oldHotCells = hotCells;
        hotCells = new HashMap<>();
        final int changesIndex = changeListModel.getChangesIndex();
        int i = changesIndex - HOT_LENGTH;
        int j = 0;
        if (i < 0) {
            j -= i;
            i = 0;
        }
        for (; i < changesIndex; i++, j++) {
            hotCells = changeListModel.getElementAt(i).heat(this, columnIndices,
                    hotCells, HOT_COLORS[j]);
        }
        for (final IntPoint point : oldHotCells.keySet()) {
            fireTableCellUpdated(point.getY(), point.getX());
        }
    }

    private int getColumnIndex(final int bit) {
        final int player = bit >> 3;
        if (!viewPlayers[player]) {
            return -1;
        }
        int count = 0;
        for (int i = player - 1; i >= 0; i--) {
            if (viewPlayers[i]) {
                count++;
            }
        }
        return 2 + ((count << 3) | (bit & 7));
    }

    public int getHeadIndex() {
        return headIndex;
    }

    public void setHeadIndex(final int headIndex) {
        final int oldHeadIndex = this.headIndex;
        this.headIndex = headIndex;
        fireTableRowsUpdated(oldHeadIndex, oldHeadIndex);
        fireTableRowsUpdated(headIndex, headIndex);
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(final int lastIndex) {
        final int oldLastIndex = this.lastIndex;
        this.lastIndex = lastIndex;
        fireTableRowsUpdated(oldLastIndex, oldLastIndex);
        fireTableRowsUpdated(lastIndex, lastIndex);
    }

    public int getTailIndex() {
        return tailIndex;
    }

    public void setTailIndex(final int tailIndex) {
        this.tailIndex = tailIndex;
        fireTableRowsUpdated(tailIndex, getRowCount() - 1);
    }

    @Override
    public void fireTableCellUpdated(int row, int column) {
        if (row >= 0 && column >= 0 && row < getRowCount()
                && column < getColumnCount()) {
            super.fireTableCellUpdated(row, column);
        }
    }

    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        final int lastRowIndex = getRowCount() - 1;
        if (lastRowIndex < 0) {
            return;
        }
        firstRow = clamp(firstRow, 0, lastRowIndex);
        lastRow = clamp(lastRow, 0, lastRowIndex);
        if (firstRow <= lastRow) {
            super.fireTableRowsUpdated(firstRow, lastRow);
        }
    }

    public Set<Int> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(final Set<Int> bookmarks) {

        final Set<Int> priorBookmarks = this.bookmarks;
        this.bookmarks = bookmarks;
        for (final Int i : bookmarks) {
            fireTableCellUpdated(i.value, 0);
        }
        for (final Int i : priorBookmarks) {
            fireTableCellUpdated(i.value, 0);
        }
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(final Movie movie) {
        this.movie = movie;
        if (movie == null) {
            headIndex = tailIndex = -1;
        } else {
            headIndex = tailIndex = movie.frameIndex;
        }
        fireTableDataChanged();
    }

    public int getPlayers() {
        return players;
    }

    public boolean[] getViewPlayers() {
        return viewPlayers;
    }

    public final void setViewPlayers(final boolean[] viewPlayers) {
        this.viewPlayers = viewPlayers;
        players = 0;
        for (int i = 0, j = 0; i < viewPlayers.length; i++) {
            if (viewPlayers[i]) {
                playerIndices[j++] = i << 3;
                players++;
            }
        }
        columnCount = 2 + (players << 3);
        for (int i = columnIndices.length - 1; i >= 0; i--) {
            columnIndices[i] = getColumnIndex(i);
        }
        fireTableStructureChanged();
        updateHotCells();
    }

    public Map<IntPoint, Color> getHotCells() {
        return hotCells;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        if (columnIndex == 0) {
            return "";
        } else if (columnIndex == 1) {
            return "Frame";
        } else {
            return BUTTON_NAMES[(columnIndex - 2) & 7];
        }
    }

    public boolean isEmpty() {
        return getRowCount() == 0;
    }

    @Override
    public int getRowCount() {
        return movie != null ? movie.getFrameCount() : 0;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    private MovieBlock getMovieBlock(final int rowIndex) {
        return movie.movieBlocks.get(rowIndex >= 0 ? (rowIndex >> BLOCK_SHIFT) : 0);
    }

    public Color getForegroundAt(final int rowIndex, final int columnIndex) {
        return Color.BLACK;
    }

    public int getButtons(final int rowIndex) {
        return rowIndex < getRowCount()
                ? getMovieBlock(rowIndex).buttons[rowIndex & BLOCK_MASK] : 0;
    }

    public void setButtons(final int rowIndex, final int buttons) {
        if (rowIndex < getRowCount()) {
            getMovieBlock(rowIndex).buttons[rowIndex & BLOCK_MASK] = buttons;
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {

        if (columnIndex == 0) {
            key.setValue(rowIndex);
            return HTMLS[(bookmarks.contains(key) ? 4 : 0)
                    | (rowIndex == lastIndex ? 2 : 0) | (rowIndex == headIndex ? 1 : 0)];
        } else if (columnIndex == 1) {
            return String.format("% 7d", rowIndex);
        }

        if (movie == null) {
            return "";
        }

        final int col = columnIndex - 2;
        final int bit = col & 7;

        return getBitBool(getMovieBlock(rowIndex).buttons[rowIndex & BLOCK_MASK],
                playerIndices[col >> 3] | bit) ? BUTTON_NAMES[bit] : "";
    }
}
