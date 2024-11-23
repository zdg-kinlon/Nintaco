package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

import static nintaco.gui.historyeditor.HistoryTableModel.createRange;

public class RunChange extends HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    private final int priorHeadIndex;
    private final int[] priorButtons;
    private final InsertChange insertChange;

    public RunChange(final int priorHeadIndex, final int currentHeadIndex,
                     final int[] priorButtons, final InsertChange insertChange) {

        this.priorHeadIndex = priorHeadIndex;
        this.priorButtons = priorButtons;
        this.insertChange = insertChange;

        setDescription(createRange("Run", priorHeadIndex, currentHeadIndex));
    }

    private void swapButtons(final HistoryTableModel model) {
        if (priorButtons != null) {
            for (int i = priorButtons.length - 1; i >= 0; i--) {
                final int rowIndex = priorHeadIndex + i;
                final int buttons = model.getButtons(rowIndex);
                model.setButtons(rowIndex, priorButtons[i]);
                priorButtons[i] = buttons;
            }
        }
    }

    @Override
    public int apply(final HistoryTableModel model) {

        swapButtons(model);
        if (insertChange != null) {
            insertChange.apply(model);
        }

        return priorHeadIndex;
    }

    @Override
    public int revert(final HistoryTableModel model) {

        if (insertChange != null) {
            insertChange.revert(model);
        }
        swapButtons(model);

        return priorHeadIndex;
    }

    @Override
    public Map<IntPoint, Color> heat(final HistoryTableModel model,
                                     final int[] columnIndices, final Map<IntPoint, Color> hotCells,
                                     final Color color) {
        hotCells.clear();
        return hotCells;
    }
}
