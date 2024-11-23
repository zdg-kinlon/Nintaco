package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;
import nintaco.movie.Movie;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InsertChange extends RangeChange implements Serializable {

    private static final long serialVersionUID = 0L;

    public InsertChange(final int rowIndex, final int[] buttons) {
        super(rowIndex, buttons);
    }

    @Override
    public int apply(final HistoryTableModel model) {
        final Movie movie = model.getMovie();
        if (movie != null) {
            movie.clearCachedFrames();
        }
        return insert(model);
    }

    @Override
    public int revert(final HistoryTableModel model) {
        final Movie movie = model.getMovie();
        if (movie != null) {
            movie.clearCachedFrames();
        }
        return delete(model);
    }

    @Override
    public Map<IntPoint, Color> heat(final HistoryTableModel model,
                                     final int[] columnIndices, final Map<IntPoint, Color> hotCells,
                                     final Color color) {

        final Map<IntPoint, Color> hcs = new HashMap<>();
        for (final Iterator<Map.Entry<IntPoint, Color>> i
             = hotCells.entrySet().iterator(); i.hasNext(); ) {
            final Map.Entry<IntPoint, Color> entry = i.next();
            final IntPoint p = entry.getKey();
            if (p.y >= rowIndex) {
                hcs.put(new IntPoint(p.x, p.y + buttons.length), entry.getValue());
            } else {
                hcs.put(p, entry.getValue());
            }
        }

        for (int i = buttons.length - 1; i >= 0; i--) {
            final int row = rowIndex + i;
            int b = buttons[i];
            for (int j = 0; j < 32; j++) {
                final int col = columnIndices[j];
                if (col >= 0 && (b & 1) == 1) {
                    hcs.put(new IntPoint(col, row), color);
                    model.fireTableCellUpdated(row, col);
                }
                b >>= 1;
            }
        }

        return hcs;
    }
}
