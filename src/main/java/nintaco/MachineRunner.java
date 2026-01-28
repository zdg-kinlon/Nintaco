package nintaco;

import nintaco.api.local.LocalAPI;
import nintaco.apu.APU;
import nintaco.apu.SystemAudioProcessor;
import nintaco.cheats.GameCheats;
import nintaco.cpu.CPU;
import nintaco.disassembler.TraceLogger;
import nintaco.gui.image.ImagePane;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.gui.rob.RobController;
import nintaco.gui.rob.RobGame;
import nintaco.gui.rob.RobState;
import nintaco.input.DeviceMapper;
import nintaco.input.InputUtil;
import nintaco.input.OtherInput;
import nintaco.mappers.Mapper;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieBlock;
import nintaco.movie.MovieFrame;
import nintaco.netplay.client.NetplayClient;
import nintaco.netplay.protocol.ControllerInput;
import nintaco.netplay.server.NetplayServer;
import nintaco.task.Task;

import java.util.List;

import static nintaco.PauseStepType.*;
import static nintaco.movie.Movie.BLOCK_MASK;
import static nintaco.movie.Movie.BLOCK_SIZE;
import static nintaco.netplay.protocol.MessageType.*;
import static nintaco.util.CollectionsUtil.convertToArray;
import static nintaco.util.GuiUtil.suppressScreensaver;
import static nintaco.util.StreamUtil.readObject;
import static nintaco.util.ThreadUtil.threadWait;
import static nintaco.util.TimeUtil.sleep;

public class MachineRunner extends Task {

    protected static final int JSR = 0x20;
    protected static final int PLP = 0x28;
    protected static final int RTI = 0x40;
    protected static final int RTS = 0x60;
    protected static final int PLA = 0x68;

    protected final ControllerInput controllerInput = new ControllerInput();

    protected volatile boolean forwardTime = true;
    protected volatile int noStepPause;
    protected volatile boolean stepPause;
    protected volatile boolean pauseRequested;
    protected volatile boolean paused;
    protected volatile PauseStepType pauseStepType = None;
    protected volatile int stepToValue;
    protected volatile int stepToValue2;
    protected volatile Thread runningThread;
    protected volatile TraceLogger traceLogger;

    protected volatile MovieBlock currentMovieBlock;
    protected volatile MovieBlock movieBlock;
    protected volatile Movie movie;
    protected volatile Movie rewindMovie;
    protected int generatingOffset;
    protected int generatingIndex;
    protected int renderingIndex;

    protected volatile NetplayClient netplayClient;
    protected volatile NetplayServer netplayServer;

    protected volatile LocalAPI localAPI;

    protected Machine machine;
    protected Mapper mapper;
    protected CPU cpu;
    protected PPU ppu;
    protected APU apu;

    protected transient volatile Breakpoint[] breakpoints;

    protected boolean terminated = true;

    public MachineRunner(final Machine machine) {
        setMachine(machine);
    }

    public void setMachine(final Machine machine) {
        if (machine == null) {
            this.machine = null;
            this.mapper = null;
            this.cpu = null;
            this.ppu = null;
            this.apu = null;
        } else {
            this.machine = machine;
            this.mapper = machine.getMapper();
            this.cpu = machine.cpu();
            this.ppu = machine.getPPU();
            this.apu = machine.getAPU();
        }
    }

    public Machine getMachine() {
        return machine;
    }

    public CPU getCPU() {
        return cpu;
    }

    public PPU getPPU() {
        return ppu;
    }

    public APU getAPU() {
        return apu;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setCurrentMovieBlock(final MovieBlock currentMovieBlock) {
        this.currentMovieBlock = currentMovieBlock;
    }

    public MovieBlock getCurrentMovieBlock() {
        return currentMovieBlock;
    }

    public void setMovieBlock(final MovieBlock movieBlock) {
        this.movieBlock = movieBlock;
    }

    public MovieBlock getMovieBlock() {
        return movieBlock;
    }

    public void setMovie(final Movie movie) {
        this.movie = movie;
        if (movie == null) {
            setForwardTime(true);
            ppu.setMachineRunner(null);
        } else {
            ppu.setMachineRunner(this);
        }
    }

    public NetplayClient getClient() {
        return netplayClient;
    }

    public void setClient(final NetplayClient netplayClient) {
        this.netplayClient = netplayClient;
    }

    public void clearClient() {
        setClient(null);
    }

    public NetplayServer getServer() {
        return netplayServer;
    }

    public void setServer(final NetplayServer netplayServer) {
        this.netplayServer = netplayServer;
    }

    public void clearServer() {
        setServer(null);
    }

    public LocalAPI getLocalAPI() {
        return localAPI;
    }

    public void setLocalAPI(final LocalAPI localAPI) {
        this.localAPI = localAPI;
    }

    public void clearLocalAPI() {
        setLocalAPI(null);
    }

    public void setBreakpoints(final List<Breakpoint> breakpoints) {
        final Breakpoint[] bs = convertToArray(Breakpoint.class, breakpoints);
        if (bs == null) {
            this.breakpoints = null;
            cpu.setBreakpoints(null);
        } else {
            for (int i = bs.length - 1; i >= 0; i--) {
                bs[i] = new Breakpoint(bs[i]);
            }
            this.breakpoints = bs;
            cpu.setBreakpoints(bs);
        }
    }

    public void setTraceLogger(TraceLogger traceLogger) {
        this.traceLogger = traceLogger;
    }

    public void frameRendered(final int[] screen) {
        final Movie m = movie;
        if (m != null) {
            m.updateMovieFrame(screen, machine);
        }
    }

    public void setForwardTime(final boolean forwardTime) {
        final Movie m = movie;
        this.forwardTime = m == null
                || (netplayClient == null && m.getMovieBlocks().isEmpty())
                ? true : forwardTime;
    }

    public boolean isForwardTime() {
        return forwardTime;
    }

    protected void applyInputs(final MovieBlock movieBlock,
                               final int frameIndex) {
        machine.getPPU().setZapper(null);
        final DeviceMapper[] mappers = mapper.getDeviceMappers();
        for (int i = mappers.length - 1; i >= 0; i--) {
            mappers[i].setMachine(machine);
        }
        mapper.updateButtons(movieBlock.buttons[frameIndex]);
        if (movieBlock.otherInputs != null) {
            final OtherInput[] otherInputs = movieBlock.otherInputs[frameIndex];
            if (otherInputs != null) {
                for (final OtherInput otherInput : otherInputs) {
                    otherInput.run(machine);
                }
            }
        }
    }

    public void runFrame(final MovieBlock movieBlock, final int frameIndex) {
        runFrame(movieBlock, frameIndex, 0L);
    }

    public long runFrame(final MovieBlock movieBlock, final int frameIndex,
                         long next) {

        applyInputs(movieBlock, frameIndex);
        while (ppu.frameRendering) {
            cpu.executeInstruction();
        }
        ppu.frameRendering = true;
        mapper.handleFrameRendered();

        return next;
    }

    @Override
    public void loop() {
        try {
            setTerminated(false);
            runningThread = Thread.currentThread();
            App.fireStepPausedChanged(stepPause);

            while (running) {
                if (forwardTime) {
                    play();
                } else {
                    rewind();
                }
            }
        } finally {
            App.setMachineRunner(null);
            App.updateFrames(null);
            setTerminated(true);
        }
    }

    protected synchronized void setTerminated(final boolean terminated) {
        this.terminated = terminated;
        notifyAll();
    }

    protected synchronized void waitForTermination() {
        while (!terminated) {
            threadWait(this);
        }
    }

    protected void play() {
        final RobController rob = ppu.getRob();
        if (rob != null) {
            App.updateRobFrame(rob);
        }
        App.setMachineRunner(this);
        App.updateFrames(this);
        final ImagePane imagePane = App.getImageFrame().getImagePane();
        imagePane.setRewinding(false);
        imagePane.setTVSystem(mapper.getTVSystem());
        GameCheats.updateMachine();

        long next = System.nanoTime();
        while (running && forwardTime) {

            suppressScreensaver();

            final NetplayClient client = netplayClient;
            if (client != null && client.isRewind()) {
                forwardTime = false;
                break;
            }

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

            if (client == null) {
                next = sleep(next, mapper);
            }

            InputUtil.pollControllers(machine);
            controllerInput.input = InputUtil.getButtons();
            controllerInput.otherInputs = InputUtil.getOtherInputs();

            final NetplayServer server = netplayServer;
            if (client != null) {
                client.writeControllerInput(controllerInput);
                client.readControllerInput(controllerInput);
            } else if (server != null) {
                server.mergeControllerInput(controllerInput);
                server.writeControllerInput(controllerInput);
            }

            final LocalAPI api = localAPI;
            if (api != null) {
                controllerInput.input = api.controllersProbed(controllerInput.input);
            }

            final Movie m = movie;
            if (m != null) {
                m.updateMovieBlock(machine, controllerInput, client == null);
            }

            mapper.updateButtons(controllerInput.input);
            if (controllerInput.otherInputs != null) {
                for (final OtherInput otherInput : controllerInput.otherInputs) {
                    otherInput.run(machine);
                }
            }
        }

        final LocalAPI api = localAPI;
        if (api != null) {
            api.machineStopped();
        }

        final NetplayServer server = netplayServer;
        if (running && !forwardTime) {
            final NetplayClient client = netplayClient;
            if (client != null) {
                final int movieFrameIndex = client.readMovieFrameIndex();
                final Movie m = movie;
                if (m != null) {
                    m.frameIndex = movieFrameIndex;
                }
            } else if (server != null) {
                final Movie m = movie;
                if (m != null) {
                    server.post(Rewind, m.frameIndex);
                }
            }
        }
    }

    protected void rewind() {
        rewindMovie = this.movie;
        final NetplayClient client = netplayClient;
        final NetplayServer server = netplayServer;
        if (rewindMovie == null
                || (client == null && rewindMovie.getMovieBlocks().isEmpty())) {
            setForwardTime(true);
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

        if (currentMovieBlock == null) {
            currentMovieBlock = removeLastMovieBlock(rewindMovie, client, server);
            movieBlock = removeLastMovieBlock(rewindMovie, client, server);
        }

        boolean reachedMovieStart = false;
        long next = System.nanoTime();
        while (running && !forwardTime) {

            suppressScreensaver();

            if (client != null && client.isPlay()) {
                forwardTime = true;
                break;
            }

            if (pauseRequested) {
                next = handlePause(next);
            }

            rewindMovie.frameIndex--;
            displayIndex--;
            generatingIndex++;

            if (displayIndex < 0) {
                final int temp = generatingOffset;
                generatingOffset = displayOffset;
                displayOffset = temp;
                for (int i = BLOCK_MASK; i >= 0; i--) {
                    rewindMovie.movieFrames[generatingOffset + i].audioLength = 0;
                }
                if (movieBlock == null) {
                    reachedMovieStart = true;
                    break;
                }
                currentMovieBlock = movieBlock;
                displayIndex = BLOCK_MASK;
                movieBlock = removeLastMovieBlock(rewindMovie, client, server);
                setMachine(null);
                if (movieBlock != null) {
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

            if (client == null) {
                next = sleep(next, mapper);
                if (server != null) {
                    server.post(FrameEnd);
                }
            } else {
                client.readFrameEnd();
            }
        }

        while (running && reachedMovieStart && !forwardTime) {

            InputUtil.rewindPollControls(machine);

            if (pauseRequested) {
                next = handlePause(next);
            }

            if (client == null) {
                next = sleep(next, mapper);
                if (server != null) {
                    server.post(FrameEnd);
                }
            } else {
                client.readFrameEnd();
            }
        }

        if (running && forwardTime && server != null) {
            server.post(Play);
        }

        if (machine != null) {
            while (++generatingIndex < BLOCK_SIZE) {
                next = runFrame(movieBlock, generatingIndex, next);
            }
            setMachine(null);
        }
        if (movieBlock != null) {
            if (client == null) {
                rewindMovie.movieBlocks.add(movieBlock);
            }
            movieBlock = null;
        }
        if (currentMovieBlock != null) {
            if (client == null) {
                rewindMovie.movieBlocks.add(currentMovieBlock);
            }

            try {
                generatingOffset = displayOffset;
                for (int i = BLOCK_MASK; i >= 0; i--) {
                    rewindMovie.movieFrames[generatingOffset + i].audioLength = 0;
                }
                renderingIndex = generatingIndex = 0;
                setMachine((Machine) readObject(currentMovieBlock.saveState));
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
                if (displayIndex >= 0) {
                    for (int i = 0; i < displayIndex; i++) {
                        next = runFrame(currentMovieBlock, generatingIndex++, next);
                    }
                    for (int i = displayIndex; i < BLOCK_SIZE; i++) {
                        currentMovieBlock.buttons[i] = 0;
                    }
                }

                mapper.updateButtons(0);
                SystemAudioProcessor.setMovie(rewindMovie);
            } catch (final Throwable t) {
                //t.printStackTrace();
            }
        }

        currentMovieBlock = null;
        movieBlock = null;

        apu.setAudioProcessor(systemAudioProcessor);
        ppu.setScreenRenderer(imagePane);
        if (vsDualSystem) {
            final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
            if (subMonitorFrame != null) {
                ((MainCPU) cpu).getSubPPU().setScreenRenderer(subMonitorFrame
                        .getImagePane());
            }
        }
        ppu.setMachineRunner(this);
    }

    protected MovieBlock removeLastMovieBlock(final Movie movie,
                                              final NetplayClient client, final NetplayServer server) {
        if (client == null) {
            MovieBlock movieBlock = null;
            if (movie != null) {
                final List<MovieBlock> movieBlocks = movie.movieBlocks;
                movieBlock = movieBlocks.isEmpty() ? null
                        : movieBlocks.remove(movieBlocks.size() - 1);
            }
            if (server != null) {
                server.post(MovieBlock, movieBlock);
            }
            return movieBlock;
        } else {
            return client.readMovieBlock();
        }
    }

    protected int[] rewindRender() {
        return rewindMovie.movieFrames[generatingOffset
                + (BLOCK_MASK & renderingIndex++)].screen;
    }

    protected int[] rewindRender2() {
        // Main PPU finishes rendering a frame slightly before the Sub PPU.
        return rewindMovie.movieFrames[generatingOffset
                + (BLOCK_MASK & (renderingIndex - 1))].screen2;
    }

    protected void rewindProcessOutputSample(final int value) {
        rewindMovie.movieFrames[generatingOffset + (BLOCK_MASK & generatingIndex)]
                .processOutputSample(value);
    }

    protected void rewindCaptureRobState(final Machine machine) {
        if (machine != null) {
            final RobState movieFrameRobState = rewindMovie.movieFrames[
                    generatingOffset + (BLOCK_MASK & (renderingIndex - 1))].robState;
            final RobController rob = machine.getPPU().getRob();
            if (rob == null) {
                movieFrameRobState.game = RobGame.NONE;
            } else {
                movieFrameRobState.init(rob.getState());
            }
        }
    }

    protected long handlePause(final long next) {
        if (shouldPause()) {
            synchronized (this) {
                if (shouldPause()) {
                    setPaused(true);
                    do {
                        threadWait(this);
                    } while (shouldPause());
                    setPaused(false);
                    return System.nanoTime();
                }
            }
        }
        return next;
    }

    protected void setPaused(final boolean paused) {
        this.paused = paused;
        notifyAll();
        App.firePauseChanged(paused);
    }

    protected boolean shouldPause() {

        if (!running) {
            return false;
        } else if (noStepPause > 0) {
            return true;
        } else if (stepPause) {
            switch (pauseStepType) {
                case None:
                    return true;
                case Frame:
                    return ppu.getFrameCounter() == stepToValue;
                case Into:
                    return cpu.state().instructionsCounter() == stepToValue;
                case Out:
                    switch (mapper.peekCpuMemory(cpu.register().pc())) {
                        case PLP:
                        case PLA: {
                            final int stackSize = 0xFF - cpu.register().s() - 1;
                            if (stackSize < stepToValue) {
                                stepToValue = stackSize;
                            }
                            break;
                        }
                        case RTI:
                        case RTS:
                            if (0xFF - cpu.register().s() - 2 < stepToValue) {
                                stepToValue = cpu.state().instructionsCounter() + 1;
                                pauseStepType = Into;
                            }
                            break;
                    }
                    return false;
                case Address:
                    return cpu.register().pc() == stepToValue;
                case Scanline:
                    return ppu.getFrameCounter() == stepToValue2
                            && ppu.getScanline() >= stepToValue;
                case Dot:
                    return ppu.getScanlineCycle() >= stepToValue
                            && ppu.getScanline() != stepToValue2;
                case Sprite0:
                    return ppu.getFrameCounter() == stepToValue && ppu.isSprite0Hit();
                case Opcode:
                    if (stepToValue2 == 1) {
                        stepToValue2 = 0;
                    } else {
                        return mapper.peekCpuMemory(cpu.register().pc()) == stepToValue;
                    }
                case NMI:
                    if (stepToValue == 1) {
                        stepToValue = cpu.interrupt().serviced() == ServicedType.NMI ? 1 : 0;
                        break;
                    } else {
                        return cpu.interrupt().serviced() == ServicedType.NMI;
                    }
                case IRQ:
                    if (stepToValue == 1) {
                        stepToValue = cpu.interrupt().serviced() == ServicedType.IRQ ? 1 : 0;
                        break;
                    } else {
                        return cpu.interrupt().serviced() == ServicedType.IRQ;
                    }
                case BRK:
                    if (stepToValue == 1) {
                        stepToValue = cpu.interrupt().serviced() == ServicedType.BRK ? 1 : 0;
                        break;
                    } else {
                        return cpu.interrupt().serviced() == ServicedType.BRK;
                    }
                case RST:
                    if (stepToValue == 1) {
                        stepToValue = cpu.interrupt().serviced() == ServicedType.RST ? 1 : 0;
                        break;
                    } else {
                        return cpu.interrupt().serviced() == ServicedType.RST;
                    }
            }
        }

        return false;
    }

    public synchronized void setNoStepPause(final boolean noStepPause) {
        setNoStepPause(noStepPause, true);
    }

    public synchronized void setNoStepPause(final boolean noStepPause,
                                            final boolean waitUntilPaused) {
        if (noStepPause) {
            this.noStepPause++;
        } else {
            this.noStepPause--;
            if (this.noStepPause < 0) {
                this.noStepPause = 0;
            }
        }

        this.pauseRequested = this.noStepPause > 0 || this.stepPause;
        notifyAll();
        if (running && noStepPause && runningThread != null) {
            runningThread.interrupt();
            if (waitUntilPaused && Thread.currentThread() != runningThread) {
                waitUntilPaused();
            }
        }
    }

    public synchronized void setStepPause(final boolean stepPause) {
        this.pauseStepType = None;
        this.stepPause = stepPause;
        this.pauseRequested = this.noStepPause > 0 || this.stepPause;
        notifyAll();
        if (running && stepPause && runningThread != null) {
            runningThread.interrupt();
        }
        App.fireStepPausedChanged(stepPause);
    }

    public synchronized void step(PauseStepType pauseStepType) {
        if (paused) {
            switch (pauseStepType) {
                case Frame:
                    stepToValue = ppu.getFrameCounter() + 1;
                    break;
                case Into:
                    stepToValue = cpu.state().instructionsCounter() + 1;
                    break;
                case Out: {
                    final int opCode = mapper.peekCpuMemory(cpu.register().pc());
                    if (opCode == RTI || opCode == RTS) {
                        pauseStepType = Into;
                        stepToValue = cpu.state().instructionsCounter() + 1;
                    } else {
                        stepToValue = 0xFF - cpu.register().s();
                    }
                    break;
                }
                case Over: {
                    final int opCode = mapper.peekCpuMemory(cpu.register().pc());
                    if (opCode == JSR) {
                        pauseStepType = Address;
                        stepToValue = (cpu.register().pc() + 3) & 0xFFFF;
                    } else {
                        pauseStepType = Into;
                        stepToValue = cpu.state().instructionsCounter() + 1;
                    }
                    break;
                }
                case Sprite0:
                    stepToValue = ppu.getFrameCounter() + (ppu.isSprite0Hit() ? 1 : 0);
                    break;
                case NMI:
                    stepToValue = cpu.interrupt().serviced() == ServicedType.NMI ? 1 : 0;
                    break;
                case IRQ:
                    stepToValue = cpu.interrupt().serviced() == ServicedType.IRQ ? 1 : 0;
                    break;
                case BRK:
                    stepToValue = cpu.interrupt().serviced() == ServicedType.BRK ? 1 : 0;
                    break;
                case RST:
                    stepToValue = cpu.interrupt().serviced() == ServicedType.RST ? 1 : 0;
                    break;
            }
            this.pauseStepType = pauseStepType;
            notifyAll();
        }
    }

    public synchronized void stepToAddress(final int address) {
        if (paused) {
            stepToValue = address;
            step(Address);
        }
    }

    public synchronized void stepToScanline(final int scanline) {
        if (paused) {
            stepToValue = scanline;
            stepToValue2 = ppu.getFrameCounter()
                    + (ppu.getScanline() >= scanline ? 1 : 0);
            step(Scanline);
        }
    }

    public synchronized void stepToDot(final int scanlineCycle) {
        if (paused) {
            stepToValue = scanlineCycle;
            stepToValue2 = (ppu.getScanlineCycle() >= scanlineCycle)
                    ? ppu.getScanline() : -2;
            step(Dot);
        }
    }

    public synchronized void stepToOpcode(final int opcode) {
        if (paused) {
            stepToValue = opcode;
            stepToValue2 = mapper.peekCpuMemory(cpu.register().pc()) == opcode ? 1 : 0;
            step(Opcode);
        }
    }

    public synchronized void stepToInstructions(final int instructions) {
        if (paused) {
            stepToValue = cpu.state().instructionsCounter() + instructions;
            pauseStepType = Into;
            notifyAll();
        }
    }

    public synchronized void kill() {
        noStepPause = 0;
        stepPause = false;
        running = false;
        canceled = true;
        notifyAll();
    }

    public synchronized void waitUntilPaused() {
        while (running && !paused) {
            threadWait(this);
        }
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public void dispose() {
        cancel();
        waitForTermination();
    }

    @Override
    public void cancel() {
        App.disposeTraceLogger();
        if (running) {
            kill();
        }
    }
}
