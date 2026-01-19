package nintaco.gui.historyeditor.tasks;

import nintaco.App;
import nintaco.MachineRunner;
import nintaco.gui.historyeditor.HistoryEditorFrame;
import nintaco.gui.historyeditor.HistoryTableModel;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.util.EDT;

import java.awt.*;

import static nintaco.movie.Movie.BLOCK_SIZE;

public class DisposeTask extends SaveStateTask {

    public DisposeTask(final Movie movie, final int tailIndex,
                       final int endFrameIndex, final HistoryTableModel historyTableModel,
                       final HistoryEditorFrame historyEditorFrame) {

        super(movie, tailIndex, endFrameIndex, historyTableModel,
                historyEditorFrame, BLOCK_SIZE);
    }

    @Override
    protected void saveState(final MachineRunner machineRunner) {
        EDT.async(historyEditorFrame::closeFrame);
        movie.truncate();
        machineRunner.setMovie(movie);
        machineRunner.getPPU().setScreenRenderer(App.getImageFrame()
                .getImagePane());
        if (movie.isVsDualSystem()) {
            final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
            if (subMonitorFrame != null) {
                ((MainCPU) machineRunner.getCPU()).getSubPPU().setScreenRenderer(
                        subMonitorFrame.getImagePane());
            }
        }
        machineRunner.getAPU().setAudioProcessor(App.getSystemAudioProcessor());
        App.setMachineRunner(machineRunner);
        App.updateFrames(machineRunner);
        new Thread(machineRunner).start();
    }

    @Override
    public void processSaveState(byte[] saveState) {
    }
}
