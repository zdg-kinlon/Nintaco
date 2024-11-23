package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static nintaco.util.CollectionsUtil.convertToArray;

public class Multichange<T extends HistoryChange>
        extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    private final T[] changes;

    public Multichange(final List<T> changes) {
        this(convertToArray(changes));
    }

    public Multichange(final T[] changes) {
        this.changes = changes;
    }

    @Override
    public int apply(final HistoryTableModel model) {
        int rowIndex = Integer.MAX_VALUE;
        for (int i = 0; i < changes.length; i++) {
            final int row = changes[i].apply(model);
            if (row >= 0 && row < rowIndex && row < model.getRowCount()) {
                rowIndex = row;
            }
        }
        return rowIndex;
    }

    @Override
    public int revert(final HistoryTableModel model) {
        int rowIndex = Integer.MAX_VALUE;
        for (int i = changes.length - 1; i >= 0; i--) {
            final int row = changes[i].revert(model);
            if (row >= 0 && row < rowIndex && row < model.getRowCount()) {
                rowIndex = row;
            }
        }
        return rowIndex;
    }

    @Override
    public Map<IntPoint, Color> heat(final HistoryTableModel model,
                                     final int[] columnIndices, Map<IntPoint, Color> hotCells,
                                     final Color color) {

        for (int i = 0; i < changes.length; i++) {
            hotCells = changes[i].heat(model, columnIndices, hotCells, color);
        }

        return hotCells;
    }
}
