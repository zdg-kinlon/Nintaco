package nintaco.gui.historyeditor.tasks;

import nintaco.App;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.gui.historyeditor.HistoryEditorFrame;
import nintaco.gui.historyeditor.HistoryTableModel;
import nintaco.input.InputUtil;
import nintaco.mappers.Mapper;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;
import nintaco.movie.MovieFrame;
import nintaco.task.Task;

import static java.lang.Math.max;
import static nintaco.movie.Movie.*;
import static nintaco.util.MathUtil.clamp;
import static nintaco.util.StreamUtil.readObject;
import static nintaco.util.StreamUtil.toByteArrayOutputStream;

public abstract class SaveStateTask extends Task {

    protected final Movie movie;
    protected final int endFrameIndex;
    protected final HistoryEditorFrame historyEditorFrame;
    protected final HistoryTableModel historyTableModel;

    protected int frameIndex;
    protected int renderIndex;

    public SaveStateTask(final Movie movie, final int tailIndex,
                         final int endFrameIndex, final HistoryTableModel historyTableModel,
                         final HistoryEditorFrame historyEditorFrame) {
        this(movie, tailIndex, endFrameIndex, historyTableModel, historyEditorFrame,
                0);
    }

    public SaveStateTask(final Movie movie, final int tailIndex,
                         final int endFrameIndex, final HistoryTableModel historyTableModel,
                         final HistoryEditorFrame historyEditorFrame,
                         final int frameIndexOffset) {
        this.movie = movie;
        this.endFrameIndex = max(0, endFrameIndex);
        this.historyEditorFrame = historyEditorFrame;
        this.historyTableModel = historyTableModel;

        frameIndex = clamp(endFrameIndex - frameIndexOffset, 0, max(0, tailIndex))
                & ~BLOCK_MASK;
        renderIndex = frameIndex;
    }

    protected void saveState(final MachineRunner machineRunner) {
        byte[] saveState = null;
        try {
            saveState = toByteArrayOutputStream(machineRunner.getMachine())
                    .toByteArray();
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
        machineRunner.dispose();
        if (saveState != null) {
            processSaveState(saveState);
        }
    }

    public abstract void processSaveState(final byte[] saveState);

    public int getFrameIndex() {
        return frameIndex;
    }

    @Override
    public void loop() {

        if (!running || movie == null) {
            return;
        }

        int blockIndex = frameIndex >> BLOCK_SHIFT;
        MovieBlock movieBlock = movie.movieBlocks.get(blockIndex);
        if (movieBlock.saveState == null) {
            return;
        }
        final Machine machine;
        try {
            machine = (Machine) readObject(movieBlock.saveState);
        } catch (final Throwable t) {
            //t.printStackTrace();
            return;
        }
        final MachineRunner machineRunner = new MachineRunner(machine);
        final Mapper mapper = machine.getMapper();
        InputUtil.setMachine(machine);
        mapper.restore(App.getCartFile());
        mapper.restore(App.getFdsFile());
        mapper.restore(App.getNsfFile());
        machine.getPPU().setScreenRenderer(this::render);
        if (movie.isVsDualSystem()) {
            ((MainCPU) machine.cpu()).getSubPPU().setScreenRenderer(
                    this::render2);
        }
        machine.getAPU().setAudioProcessor(this::processOutputSample);

        while (running && frameIndex <= endFrameIndex) {
            final int buttonIndex = frameIndex & BLOCK_MASK;
            if (buttonIndex == 0) {
                blockIndex = frameIndex >> BLOCK_SHIFT;
                movieBlock = movie.movieBlocks.get(blockIndex);
            }
            final MovieFrame movieFrame = movie.movieFrames[DOUBLE_MASK & frameIndex];
            movieFrame.frameIndex = frameIndex;
            movieFrame.audioLength = 0;
            machineRunner.runFrame(movieBlock, buttonIndex);
            historyEditorFrame.setProgressBar(this, frameIndex);
            frameIndex++;
        }

        if (running) {
            saveState(machineRunner);
        }
    }

    protected void processOutputSample(final int value) {
        movie.movieFrames[DOUBLE_MASK & frameIndex].processOutputSample(value);
    }

    protected int[] render() {
        return movie.movieFrames[DOUBLE_MASK & renderIndex++].screen;
    }

    protected int[] render2() {
        // Main PPU finishes rendering a frame slightly before the Sub PPU.
        return movie.movieFrames[DOUBLE_MASK & (renderIndex - 1)].screen2;
    }
}
