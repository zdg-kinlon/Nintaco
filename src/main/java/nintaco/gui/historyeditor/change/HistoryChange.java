package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

import static nintaco.util.StringUtil.getTimeString;

public abstract class HistoryChange implements Serializable {

    private static final long serialVersionUID = 0L;

    private String description;

    public abstract int apply(HistoryTableModel model);

    public abstract int revert(HistoryTableModel model);

    public abstract Map<IntPoint, Color> heat(HistoryTableModel model,
                                              int[] columnIndices, Map<IntPoint, Color> hotCells, Color color);

    public void setDescription(String description, Object... params) {
        this.description = String.format("%s %s", getTimeString(),
                String.format(description, params));
    }

    @Override
    public String toString() {
        return description;
    }
}
