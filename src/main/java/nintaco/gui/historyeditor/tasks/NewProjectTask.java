package nintaco.gui.historyeditor.tasks;

import nintaco.gui.historyeditor.HistoryEditorFrame;
import nintaco.gui.historyeditor.HistoryTableModel;
import nintaco.movie.Movie;

import java.awt.*;

public class NewProjectTask extends SaveStateTask {

    public NewProjectTask(final Movie movie, final int tailIndex,
                          final int endFrameIndex, final HistoryTableModel historyTableModel,
                          final HistoryEditorFrame historyEditorFrame) {

        super(movie, tailIndex, endFrameIndex, historyTableModel,
                historyEditorFrame);
    }

    @Override
    public void processSaveState(final byte[] saveState) {
        EventQueue.invokeLater(() -> {
            historyEditorFrame.createNewProject(saveState);
        });
    }
}
