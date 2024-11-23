package nintaco.gui.historyeditor.tasks;

import nintaco.App;
import nintaco.Breakpoint;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.apu.SystemAudioProcessor;
import nintaco.cheats.GameCheats;
import nintaco.disassembler.TraceLogger;
import nintaco.gui.IntList;
import nintaco.gui.historyeditor.HistoryEditorFrame;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.ImagePane;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.gui.rob.RobController;
import nintaco.input.InputUtil;
import nintaco.input.OtherInput;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;
import nintaco.movie.MovieFrame;
import nintaco.util.CollectionsUtil;

import java.util.List;

import static java.lang.Math.max;
import static nintaco.movie.Movie.*;
import static nintaco.util.GuiUtil.suppressScreensaver;
import static nintaco.util.StreamUtil.readObject;
import static nintaco.util.StreamUtil.toByteArrayOutputStream;
import static nintaco.util.TimeUtil.sleep;

public class RecordTask extends MachineRunner {

    private final HistoryEditorFrame historyEditorFrame;
    private final IntList priorButtons = new IntList();

    private int startFrameIndex;
    private int priorLastFrameIndex;

    private volatile int buttonsMask;
    private volatile boolean recordOther;
    private volatile boolean mergeButtons;

    private int renderIndex;

    public RecordTask(final Movie movie, final int startFrameIndex,
                      final HistoryEditorFrame historyEditorFrame) {
        super(null);
        this.movie = movie;
        this.startFrameIndex = max(0, startFrameIndex);
        this.historyEditorFrame = historyEditorFrame;
        this.priorLastFrameIndex = movie.getFrameCount() - 1;
    }

    public void setRecordOptions(final boolean[] recordPlayers,
                                 final boolean recordOther, final boolean mergeButtons) {

        int mask = 0;
        for (int i = recordPlayers.length - 1; i >= 0; i--) {
            mask <<= 8;
            mask |= recordPlayers[i] ? 0xFF : 0x00;
        }
        this.buttonsMask = mask;
        this.recordOther = recordOther;
        this.mergeButtons = mergeButtons;
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

        App.handleFrameRendered(this);
        return next;
    }

    @Override
    public void loop() {
        try {
            setTerminated(false);
            runningThread = Thread.currentThread();
            if (running && movie != null && startFrameIndex >= 0) {
                App.fireStepPausedChanged(stepPause);

                while (running) {
                    if (forwardTime) {
                        play();
                    } else {
                        rewind();
                    }
                }

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

        if (startFrameIndex < 0) {
            startFrameIndex = 0;
        }

        final boolean vsDualSystem = movie.isVsDualSystem();
        movie.frameIndex = startFrameIndex & ~BLOCK_MASK;
        renderIndex = movie.frameIndex;
        int blockIndex = movie.frameIndex >> BLOCK_SHIFT;

        movieBlock = movie.movieBlocks.get(blockIndex);
        if (movieBlock.saveState == null) {
            return;
        }
        try {
            setMachine((Machine) readObject(movieBlock.saveState));
        } catch (final Throwable t) {
            //t.printStackTrace();
            return;
        }
        InputUtil.setMachine(machine);
        mapper.restore(App.getCartFile());
        mapper.restore(App.getFdsFile());
        mapper.restore(App.getNsfFile());
        ppu.setScreenRenderer(this::recordRender);
        if (vsDualSystem) {
            ((MainCPU) cpu).getSubPPU().setScreenRenderer(this::recordRender2);
        }
        apu.setAudioProcessor(this::recordProcessOutputSample);

        final ImageFrame imageFrame = App.getImageFrame();
        final ImagePane imagePane = imageFrame.getImagePane();
        imagePane.setRewinding(false);
        final SystemAudioProcessor systemAudioProcessor
                = App.getSystemAudioProcessor();

        final RobController rob = ppu.getRob();
        if (rob != null) {
            App.updateRobFrame(rob);
        }
        App.setMachineRunner(this);
        App.updateFrames(this);
        App.getImageFrame().getImagePane().setTVSystem(mapper.getTVSystem());
        GameCheats.updateMachine();

        long next = System.nanoTime();
        while (true) {

            suppressScreensaver();

            final MovieFrame movieFrame
                    = movie.movieFrames[DOUBLE_MASK & movie.frameIndex];
            movieFrame.frameIndex = movie.frameIndex;
            movieFrame.audioLength = 0;

            next = runFrame(movieBlock, movie.frameIndex & BLOCK_MASK, next);

            if (movie.frameIndex >= startFrameIndex) {
                historyEditorFrame.handleRecordedFrame(this, movie.frameIndex,
                        movie.frameIndex > priorLastFrameIndex);
                imagePane.render(movieFrame.screen);
                if (vsDualSystem) {
                    final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
                    if (subMonitorFrame != null) {
                        subMonitorFrame.getImagePane().render(movieFrame.screen2);
                    }
                }
                systemAudioProcessor.processOutputSamples(movieFrame.audioSamples,
                        movieFrame.audioLength);
            }

            if (!(running && forwardTime)) {
                break;
            }

            if (++movie.frameIndex >= movie.frameCount) {
                movie.frameCount = movie.frameIndex + 1;
            }

            final int buttonIndex = movie.frameIndex & BLOCK_MASK;

            if (buttonIndex == 0) {
                blockIndex = movie.frameIndex >> BLOCK_SHIFT;
                while (blockIndex >= movie.movieBlocks.size()) {
                    movie.movieBlocks.add(new MovieBlock());
                }
                movieBlock = movie.movieBlocks.get(blockIndex);
                try {
                    movieBlock.saveState = toByteArrayOutputStream(machine)
                            .toByteArray();
                } catch (final Throwable t) {
                    //t.printStackTrace();
                }
            }

            if (movie.frameIndex >= startFrameIndex) {
                next = sleep(next, mapper);

                InputUtil.pollControllers(machine);

                if (movie.frameIndex <= priorLastFrameIndex) {
                    priorButtons.add(movieBlock.buttons[buttonIndex]);
                }
                final int buttons = buttonsMask & InputUtil.getButtons();
                if (mergeButtons) {
                    movieBlock.buttons[buttonIndex] |= buttons;
                } else {
                    movieBlock.buttons[buttonIndex] = (movieBlock.buttons[buttonIndex]
                            & ~buttonsMask) | buttons;
                }

                final OtherInput[] otherInputs = InputUtil.getOtherInputs();
                if (recordOther && otherInputs != null) {
                    if (movieBlock.otherInputs == null) {
                        movieBlock.otherInputs = new OtherInput[BLOCK_SIZE][];
                    }
                    if (mergeButtons) {
                        movieBlock.otherInputs[buttonIndex] = CollectionsUtil.concat(
                                OtherInput.class, movieBlock.otherInputs[buttonIndex],
                                otherInputs);
                    } else {
                        movieBlock.otherInputs[buttonIndex] = otherInputs;
                    }
                }
            }
        }

        App.updateFrames(null);
        historyEditorFrame.handleEndRecord(priorButtons.toArray());
        priorButtons.clear();
        priorLastFrameIndex = movie.getFrameCount() - 1;
    }

    @Override
    protected void rewind() {

        rewindMovie = this.movie;
        if (rewindMovie == null || rewindMovie.getMovieBlocks().isEmpty()) {
            forwardTime = true;
            return;
        }

        final boolean vsDualSystem = rewindMovie.isVsDualSystem();
        setMachine(null);
        final SystemAudioProcessor systemAudioProcessor
                = App.getSystemAudioProcessor();
        SystemAudioProcessor.setMovie(null);

        final ImagePane imagePane = App.getImageFrame().getImagePane();
        imagePane.setRewinding(true);
        int[] screen = imagePane.render();
        int[] screen2 = null;
        if (vsDualSystem) {
            final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
            if (subMonitorFrame != null) {
                screen2 = subMonitorFrame.getImagePane().render();
            }
        }

        int displayIndex = rewindMovie.frameIndex & BLOCK_MASK;
        int displayOffset = rewindMovie.frameIndex & BLOCK_SIZE;
        generatingOffset = displayOffset ^ BLOCK_SIZE;
        renderingIndex = generatingIndex = 0;

        final List<MovieBlock> movieBlocks = rewindMovie.getMovieBlocks();
        movieBlock = null;

        boolean reachedMovieStart = false;
        long next = System.nanoTime();
        while (running && !forwardTime) {

            if (pauseRequested) {
                next = handlePause(next);
            }

            rewindMovie.frameIndex--;
            displayIndex--;
            generatingIndex++;

            if (rewindMovie.frameIndex < 0) {
                reachedMovieStart = true;
                break;
            }

            if (displayIndex < 0) {
                final int temp = generatingOffset;
                generatingOffset = displayOffset;
                displayOffset = temp;
                for (int i = BLOCK_MASK; i >= 0; i--) {
                    rewindMovie.movieFrames[generatingOffset + i].audioLength = 0;
                }
                displayIndex = BLOCK_MASK;
                movieBlock = null;
                setMachine(null);
                final int movieBlocksIndex = (rewindMovie.frameIndex >> BLOCK_SHIFT) - 1;
                if (movieBlocksIndex >= 0) {
                    movieBlock = movieBlocks.get(movieBlocksIndex);
                    renderingIndex = generatingIndex = 0;
                    try {
                        setMachine((Machine) readObject(movieBlock.saveState));
                    } catch (final Throwable t) {
                        //t.printStackTrace();
                    }
                    InputUtil.setMachine(machine);
                    mapper.restore(App.getCartFile());
                    mapper.restore(App.getFdsFile());
                    mapper.restore(App.getNsfFile());
                    ppu.setScreenRenderer(this::rewindRender);
                    if (vsDualSystem) {
                        ((MainCPU) cpu).getSubPPU().setScreenRenderer(
                                this::rewindRender2);
                    }
                    apu.setAudioProcessor(this::rewindProcessOutputSample);
                }
            }
            final MovieFrame displayFrame
                    = rewindMovie.movieFrames[displayOffset + displayIndex];
            System.arraycopy(displayFrame.screen, 0, screen, 0, screen.length);
            screen = imagePane.render();
            if (vsDualSystem) {
                final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
                if (subMonitorFrame != null) {
                    if (screen2 != null) {
                        System.arraycopy(displayFrame.screen2, 0, screen2, 0,
                                screen2.length);
                    }
                    screen2 = subMonitorFrame.getImagePane().render();
                }
            }
            final int[] audioSamples = displayFrame.audioSamples;
            for (int i = displayFrame.audioLength - 1; i >= 0; i--) {
                systemAudioProcessor.processOutputSample(audioSamples[i]);
            }
            rewindCaptureRobState(machine);
            App.updateRobFrame(displayFrame.robState);
            App.updateGlassesFrame(displayFrame.screen);
            InputUtil.rewindPollControls(machine);
            if (machine != null) {
                next = runFrame(movieBlock, generatingIndex, next);
            }
            historyEditorFrame.handleRewoundFrame(this, rewindMovie.frameIndex);
            next = sleep(next, mapper);
        }

        if (machine != null) {
            while (++generatingIndex < BLOCK_SIZE) {
                next = runFrame(movieBlock, generatingIndex, next);
            }
        }

        while (running && reachedMovieStart && !forwardTime) {
            if (pauseRequested) {
                next = handlePause(next);
            }
            InputUtil.rewindPollControls(machine);
            next = sleep(next, mapper);
        }

        startFrameIndex = movie.frameIndex;
        setMachine(null);
    }

    private void recordProcessOutputSample(final int value) {
        movie.movieFrames[DOUBLE_MASK & movie.frameIndex]
                .processOutputSample(value);
    }

    private int[] recordRender() {
        return movie.movieFrames[DOUBLE_MASK & renderIndex++].screen;
    }

    private int[] recordRender2() {
        return movie.movieFrames[DOUBLE_MASK & (renderIndex - 1)].screen2;
    }
}
