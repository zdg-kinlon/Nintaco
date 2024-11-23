package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public class InitializationChange
        extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    public InitializationChange() {
        setDescription("Initialization");
    }

    @Override
    public int apply(final HistoryTableModel model) {
        return 0;
    }

    @Override
    public int revert(final HistoryTableModel model) {
        return 0;
    }

    @Override
    public Map<IntPoint, Color> heat(final HistoryTableModel model,
                                     final int[] columnIndices, final Map<IntPoint, Color> hotCells,
                                     final Color color) {
        return hotCells;
    }
}
