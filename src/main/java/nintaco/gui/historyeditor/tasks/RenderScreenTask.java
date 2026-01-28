package nintaco.gui.historyeditor.tasks;

import nintaco.App;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.input.InputUtil;
import nintaco.mappers.Mapper;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;
import nintaco.movie.MovieFrame;
import nintaco.task.Task;

import static nintaco.movie.Movie.*;
import static nintaco.util.StreamUtil.readObject;

public class RenderScreenTask extends Task {

    public static final RenderScreenListener DEFAULT_RENDER_SCREEN_LISTENER
            = (machineRunner, movieFrame) -> {
        App.getImageFrame().getImagePane().render(movieFrame.screen);
        if (movieFrame.isVsDualSystem()) {
            final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
            if (subMonitorFrame != null) {
                subMonitorFrame.getImagePane().render(movieFrame.screen2);
            }
        }
        App.updateRobFrame(movieFrame.robState);
    };

    private final Movie movie;
    private final int renderFrameIndex;
    private final RenderScreenListener renderScreenListener;

    private int frameIndex;
    private int renderIndex;

    private volatile boolean disposeEnabled = true;

    public RenderScreenTask(final Movie movie, final int renderFrameIndex,
                            final boolean renderToImagePane) {
        this(movie, renderFrameIndex, renderToImagePane
                ? DEFAULT_RENDER_SCREEN_LISTENER : null);
    }

    public RenderScreenTask(final Movie movie, final int renderFrameIndex,
                            final RenderScreenListener renderScreenListener) {
        this.movie = movie;
        this.renderFrameIndex = renderFrameIndex;
        this.renderScreenListener = renderScreenListener;
    }

    public void setDisposeEnabled(final boolean disposeEnabled) {
        this.disposeEnabled = disposeEnabled;
    }

    @Override
    public void loop() {

        if (!running || movie == null || renderFrameIndex < 0) {
            return;
        }

        frameIndex = renderFrameIndex & ~BLOCK_MASK;
        renderIndex = frameIndex;
        final int blockIndex = renderFrameIndex >> BLOCK_SHIFT;
        if (blockIndex >= movie.movieBlocks.size()) {
            return;
        }
        if (disposeEnabled && movie.movieFrames[DOUBLE_MASK & renderFrameIndex]
                .frameIndex == renderFrameIndex) {
            if (renderScreenListener != null) {
                renderScreenListener.completedRendering(null,
                        movie.movieFrames[DOUBLE_MASK & renderFrameIndex]);
            }
            return;
        }
        final MovieBlock movieBlock = movie.movieBlocks.get(blockIndex);
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

        final int completeIndex = BLOCK_MASK & renderFrameIndex;
        for (int i = 0; i < BLOCK_SIZE && running; i++, frameIndex++) {
            final MovieFrame movieFrame = movie.movieFrames[DOUBLE_MASK & frameIndex];
            movieFrame.frameIndex = frameIndex;
            movieFrame.audioLength = 0;
            machineRunner.runFrame(movieBlock, i);
            if (i == completeIndex && renderScreenListener != null) {
                renderScreenListener.completedRendering(machineRunner, movieFrame);
                if (!disposeEnabled) {
                    return;
                }
            }
        }
        if (disposeEnabled) {
            machineRunner.dispose();
        }
    }

    private void processOutputSample(final int value) {
        movie.movieFrames[DOUBLE_MASK & frameIndex].processOutputSample(value);
    }

    private int[] render() {
        return movie.movieFrames[DOUBLE_MASK & renderIndex++].screen;
    }

    private int[] render2() {
        // Main PPU finishes rendering a frame slightly before the Sub PPU.
        return movie.movieFrames[DOUBLE_MASK & (renderIndex - 1)].screen2;
    }
}
