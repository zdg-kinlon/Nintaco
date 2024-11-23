package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public class ButtonsChange extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    private final int rowIndex;
    private final int prior;
    private final int current;

    public ButtonsChange(final int rowIndex, final int prior, final int current) {

        this.rowIndex = rowIndex;
        this.prior = prior;
        this.current = current;
    }

    @Override
    public int apply(final HistoryTableModel model) {
        model.setButtons(rowIndex, current);
        return rowIndex;
    }

    @Override
    public int revert(final HistoryTableModel model) {
        model.setButtons(rowIndex, prior);
        return rowIndex;
    }

    @Override
    public Map<IntPoint, Color> heat(final HistoryTableModel model,
                                     final int[] columnIndices, final Map<IntPoint, Color> hotCells,
                                     final Color color) {

        int diff = current ^ prior;
        for (int i = 0; i < 32; i++) {
            final int col = columnIndices[i];
            if (col >= 0 && (diff & 1) == 1) {
                hotCells.put(new IntPoint(col, rowIndex), color);
                model.fireTableCellUpdated(rowIndex, col);
            }
            diff >>= 1;
        }

        return hotCells;
    }
}
