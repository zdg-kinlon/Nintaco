package nintaco.gui.historyeditor.change;

import nintaco.gui.IntPoint;
import nintaco.gui.historyeditor.HistoryTableModel;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeleteChange extends RangeChange implements Serializable {

    private static final long serialVersionUID = 0L;

    protected final int rowCount;
    protected byte[] saveState;
    protected byte[] priorSaveState;

    public DeleteChange(final int rowIndex, final int rowCount) {
        super(rowIndex, new int[rowCount]);
        this.rowCount = rowCount;
    }

    public byte[] getSaveState() {
        return saveState;
    }

    public void setSaveState(final byte[] saveState) {
        this.saveState = saveState;
    }

    @Override
    public int apply(final HistoryTableModel model) {
        final Movie movie = model.getMovie();
        List<MovieBlock> blocks = null;
        if (movie != null) {
            movie.clearCachedFrames();
            blocks = movie.getMovieBlocks();
        }
        if (saveState != null && blocks != null && !blocks.isEmpty()) {
            priorSaveState = blocks.get(0).saveState;
        }
        final int row = delete(model);
        if (saveState != null && blocks != null && !blocks.isEmpty()) {
            blocks.get(0).saveState = saveState;
        }
        return row;
    }

    @Override
    public int revert(final HistoryTableModel model) {
        final Movie movie = model.getMovie();
        List<MovieBlock> blocks = null;
        if (movie != null) {
            movie.clearCachedFrames();
            blocks = movie.getMovieBlocks();
        }
        final int row = insert(model);
        if (saveState != null && blocks != null && !blocks.isEmpty()) {
            blocks.get(0).saveState = priorSaveState;
        }
        return row;
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
            if (p.y < rowIndex) {
                hcs.put(p, entry.getValue());
            } else if (p.y >= rowIndex + rowCount) {
                hcs.put(new IntPoint(p.x, p.y - rowCount), entry.getValue());
            }
        }

        return hcs;
    }
}
