package nintaco.gui.watchhistory;

import nintaco.App;
import nintaco.Breakpoint;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.apu.SystemAudioProcessor;
import nintaco.cheats.GameCheats;
import nintaco.disassembler.TraceLogger;
import nintaco.gui.historyeditor.tasks.FramePlayedListener;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.ImagePane;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.gui.rob.RobController;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;
import nintaco.movie.MovieFrame;

import static java.lang.Math.max;
import static nintaco.movie.Movie.*;
import static nintaco.util.GuiUtil.suppressScreensaver;
import static nintaco.util.StreamUtil.readObject;
import static nintaco.util.TimeUtil.sleep;

public class WatchMovieTask extends MachineRunner {

    private final int startFrameIndex;
    private final int endFrameIndex;
    private final FramePlayedListener listener;

    private int frameIndex;
    private int renderIndex;

    public WatchMovieTask(final Movie movie, final int startFrameIndex,
                          final int endFrameIndex, final FramePlayedListener listener) {
        super(null);
        this.movie = movie;
        this.startFrameIndex = max(0, startFrameIndex);
        this.endFrameIndex = max(0, endFrameIndex);
        this.listener = listener;
    }

    @Override
    public long runFrame(final MovieBlock movieBlock, final int frameIndex,
                         long next) {

        applyInputs(movieBlock, frameIndex);
        while (ppu.frameRendering) {
            final Breakpoint[] bs = breakpoints;
            if (bs != null) {
                for (int i = bs.length - 1; i >= 0; i--) {
                    final Breakpoint breakpoint = bs[i];
                    if (breakpoint.hit) {
                        breakpoint.hit = false;
                        setStepPause(true);
                    }
                }
            }
            final TraceLogger logger = traceLogger;
            if (logger != null) {
                logger.log(true, cpu, ppu, mapper);
            }
            if (pauseRequested) {
                next = handlePause(next);
            }
            cpu.executeInstruction();
            if (logger != null) {
                logger.log(false, cpu, ppu, mapper);
            }
        }
        ppu.frameRendering = true;
        mapper.handleFrameRendered();

        final RobController rob = machine.getPPU().getRob();
        if (rob != null) {
            rob.scanMemory(machine.getMapper());
            if (rob.update()) {
                App.updateRobFrame(rob);
            }
        }

        App.handleFrameRendered(this);
        return next;
    }

    @Override
    public void loop() {

        try {
            setTerminated(false);
            runningThread = Thread.currentThread();
            if (running && movie != null && startFrameIndex >= 0
                    && endFrameIndex >= startFrameIndex) {
                App.fireStepPausedChanged(stepPause);
                play();
                cancel();
                App.setMachineRunner(null);
                App.updateFrames(null);
            }
        } finally {
            setTerminated(true);
        }
    }

    @Override
    public void play() {
        frameIndex = startFrameIndex & ~BLOCK_MASK;
        renderIndex = frameIndex;
        movieBlock = movie.movieBlocks.get(frameIndex >> BLOCK_SHIFT);
        if (movieBlock.saveState == null) {
            return;
        }
        try {
            setMachine((Machine) readObject(movieBlock.saveState));
        } catch (final Throwable t) {
            //t.printStackTrace();
            return;
        }
        final boolean vsDualSystem = machine.isVsDualSystem();
        mapper.restore(App.getCartFile());
        mapper.restore(App.getFdsFile());
        mapper.restore(App.getNsfFile());
        ppu.setScreenRenderer(this::render);
        if (vsDualSystem) {
            ((MainCPU) cpu).getSubPPU().setScreenRenderer(this::render2);
        }
        apu.setAudioProcessor(this::processOutputSample);

        final ImageFrame imageFrame = App.getImageFrame();
        final ImagePane imagePane = imageFrame.getImagePane();
        final SystemAudioProcessor systemAudioProcessor
                = App.getSystemAudioProcessor();
        imagePane.setRewinding(false);

        App.setMachineRunner(this);
        App.updateFrames(this);
        imagePane.setTVSystem(mapper.getTVSystem());
        GameCheats.updateMachine();

        long next = System.nanoTime();
        while (running && frameIndex <= endFrameIndex) {

            suppressScreensaver();

            final MovieFrame movieFrame = movie.movieFrames[DOUBLE_MASK & frameIndex];
            movieFrame.frameIndex = frameIndex;
            movieFrame.audioLength = 0;
            next = runFrame(movieBlock, frameIndex & BLOCK_MASK, next);

            if (frameIndex >= startFrameIndex) {
                listener.framePlayed(this, frameIndex, this);
                imagePane.render(movieFrame.screen);
                if (vsDualSystem) {
                    final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
                    if (subMonitorFrame != null) {
                        subMonitorFrame.getImagePane().render(movieFrame.screen2);
                    }
                }
                if (startFrameIndex != endFrameIndex) {
                    systemAudioProcessor.processOutputSamples(movieFrame.audioSamples,
                            movieFrame.audioLength);
                }
            }

            if ((++frameIndex & BLOCK_MASK) == 0) {
                final int index = frameIndex >> BLOCK_SHIFT;
                if (index < movie.movieBlocks.size()) {
                    movieBlock = movie.movieBlocks.get(index);
                } else {
                    break;
                }
            }

            if (frameIndex >= startFrameIndex) {
                next = sleep(next, mapper);
            }
        }

        App.updateFrames(null);
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
