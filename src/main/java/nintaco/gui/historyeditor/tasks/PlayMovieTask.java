package nintaco.gui.historyeditor.tasks;

import nintaco.App;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.ScreenRenderer;
import nintaco.apu.AudioProcessor;
import nintaco.apu.SystemAudioProcessor;
import nintaco.gui.image.ImagePane;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.mappers.Mapper;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;
import nintaco.movie.MovieFrame;
import nintaco.task.Task;

import static java.lang.Math.max;
import static nintaco.movie.Movie.*;
import static nintaco.util.GuiUtil.suppressScreensaver;
import static nintaco.util.StreamUtil.readObject;
import static nintaco.util.StreamUtil.toByteArrayOutputStream;
import static nintaco.util.TimeUtil.sleep;

public class PlayMovieTask extends Task {

    private final Movie movie;
    private final int startFrameIndex;
    private final int endFrameIndex;
    private final FramePlayedListener listener;
    private final AudioProcessor audioProcessor;
    private final ScreenRenderer screenRenderer;
    private final ScreenRenderer screenRenderer2;
    private final TaskTerminatedListener taskTerminatedListener;

    private volatile boolean renderingEnabled;
    private volatile boolean audioEnabled;
    private volatile boolean delayEnabled;
    private volatile boolean disposeEnabled = true;

    private FrameRenderer frameRenderer;
    private FrameRenderer frameRenderer2;

    private int frameIndex;
    private int renderIndex;

    public PlayMovieTask(final Movie movie, final int startFrameIndex,
                         final int endFrameIndex, final boolean realtime,
                         final FramePlayedListener listener) {
        this(movie, startFrameIndex, endFrameIndex, realtime, listener,
                null, null, null, null);
    }

    public PlayMovieTask(
            final Movie movie,
            final int startFrameIndex,
            final int endFrameIndex,
            final boolean realtime,
            final FramePlayedListener listener,
            final ScreenRenderer screenRenderer,
            final ScreenRenderer screenRenderer2,
            final AudioProcessor audioProcessor,
            final TaskTerminatedListener taskTerminatedListener) {

        this.movie = movie;
        this.startFrameIndex = max(0, startFrameIndex);
        this.endFrameIndex = max(0, endFrameIndex);
        this.listener = listener;
        setRealtime(realtime);
        this.screenRenderer = screenRenderer == null
                ? this::render : screenRenderer;
        this.screenRenderer2 = screenRenderer2 == null
                ? this::render2 : screenRenderer2;
        this.audioProcessor = audioProcessor == null
                ? this::processOutputSample : audioProcessor;
        this.taskTerminatedListener = taskTerminatedListener;
    }

    public void setRealtime(final boolean realtime) {
        setRenderingEnabled(realtime);
        setAudioEnabled(realtime);
        setDelayEnabled(realtime);
    }

    public void setRenderingEnabled(final boolean renderingEnabled) {
        this.renderingEnabled = renderingEnabled;
    }

    public void setAudioEnabled(final boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public void setDelayEnabled(boolean delayEnabled) {
        this.delayEnabled = delayEnabled;
    }

    public void setFrameRenderer(final FrameRenderer frameRenderer) {
        this.frameRenderer = frameRenderer;
    }

    public void setFrameRenderer2(final FrameRenderer frameRenderer2) {
        this.frameRenderer2 = frameRenderer2;
    }

    public void setDisposeEnabled(boolean disposeEnabled) {
        this.disposeEnabled = disposeEnabled;
    }

    @Override
    public void loop() {

        if (!running || movie == null || startFrameIndex < 0
                || endFrameIndex < startFrameIndex) {
            return;
        }

        final boolean vsDualSystem = movie.isVsDualSystem();
        frameIndex = startFrameIndex & ~BLOCK_MASK;
        renderIndex = frameIndex;
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
        mapper.restore(App.getCartFile());
        mapper.restore(App.getFdsFile());
        mapper.restore(App.getNsfFile());
        machine.getPPU().setScreenRenderer(screenRenderer);
        if (vsDualSystem) {
            ((MainCPU) machine.getCPU()).getSubPPU().setScreenRenderer(
                    screenRenderer2);
        }
        machine.getAPU().setAudioProcessor(audioProcessor);

        final ImagePane imagePane = App.getImageFrame().getImagePane();
        final SystemAudioProcessor systemAudioProcessor
                = App.getSystemAudioProcessor();
        long next = System.nanoTime();
        while (running && frameIndex <= endFrameIndex) {
            suppressScreensaver();
            final int buttonIndex = frameIndex & BLOCK_MASK;
            if (buttonIndex == 0) {
                blockIndex = frameIndex >> BLOCK_SHIFT;
                movieBlock = movie.movieBlocks.get(blockIndex);
                try {
                    movieBlock.saveState = toByteArrayOutputStream(machine)
                            .toByteArray();
                } catch (final Throwable t) {
                    //t.printStackTrace();
                }
            }
            final MovieFrame movieFrame = movie.movieFrames[DOUBLE_MASK & frameIndex];
            movieFrame.frameIndex = frameIndex;
            movieFrame.audioLength = 0;
            next = machineRunner.runFrame(movieBlock, buttonIndex, next);

            if (frameIndex >= startFrameIndex) {
                if (renderingEnabled) {
                    if (frameRenderer == null) {
                        imagePane.render(movieFrame.screen);
                        if (vsDualSystem) {
                            if (frameRenderer2 == null) {
                                final SubMonitorFrame subMonitorFrame
                                        = App.getSubMonitorFrame();
                                if (subMonitorFrame != null) {
                                    subMonitorFrame.getImagePane().render(movieFrame.screen2);
                                }
                            } else {
                                frameRenderer2.render(movieFrame.screen2);
                            }
                        }
                    } else {
                        frameRenderer.render(movieFrame.screen);
                    }
                }
                if (audioEnabled) {
                    systemAudioProcessor.processOutputSamples(movieFrame.audioSamples,
                            movieFrame.audioLength);
                }
                if (listener != null) {
                    listener.framePlayed(this, frameIndex, machineRunner);
                }
                if (delayEnabled) {
                    next = sleep(next, mapper);
                }
            }

            frameIndex++;
        }
        if (disposeEnabled) {
            machineRunner.dispose();
        }
        if (taskTerminatedListener != null) {
            taskTerminatedListener.taskTerminated(this);
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
