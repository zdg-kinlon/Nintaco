package nintaco.gui.historyeditor.tasks;

import nintaco.gui.historyeditor.HistoryEditorFrame;
import nintaco.gui.historyeditor.HistoryTableModel;
import nintaco.gui.historyeditor.change.DeleteChange;
import nintaco.movie.Movie;
import nintaco.util.EDT;

import java.awt.*;

public class TrimTopTask extends SaveStateTask {

    public TrimTopTask(final Movie movie, final int tailIndex,
                       final int endFrameIndex, final HistoryTableModel historyTableModel,
                       final HistoryEditorFrame historyEditorFrame) {

        super(movie, tailIndex, endFrameIndex, historyTableModel,
                historyEditorFrame);
    }

    @Override
    public void processSaveState(final byte[] saveState) {
        final DeleteChange change = new DeleteChange(0, endFrameIndex + 1);
        change.setSaveState(saveState);
        change.setDescription(HistoryTableModel.createRange("Trim top", 0,
                endFrameIndex));
        EDT.async(() -> {
            historyTableModel.addChange(change);
            historyEditorFrame.runToLastClickedRow(0);
        });
    }
}
