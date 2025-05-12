package cn.kinlon.emu;

import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.cpu.CPU;
import cn.kinlon.emu.cpu.Register;
import cn.kinlon.emu.gui.image.ImagePane;
import cn.kinlon.emu.input.ControllerInput;
import cn.kinlon.emu.input.InputUtil;
import cn.kinlon.emu.input.OtherInput;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.task.Task;

import static cn.kinlon.emu.PauseStepType.*;
import static cn.kinlon.emu.utils.GuiUtil.suppressScreensaver;
import static cn.kinlon.emu.utils.ThreadUtil.threadWait;
import static cn.kinlon.emu.utils.TimeUtil.sleep;

public class MachineRunner extends Task {

    protected static final int JSR = 0x20;
    protected static final int PLP = 0x28;
    protected static final int RTI = 0x40;
    protected static final int RTS = 0x60;
    protected static final int PLA = 0x68;

    protected final ControllerInput controllerInput = new ControllerInput();

    protected final boolean forwardTime = true;
    protected volatile int noStepPause;
    protected volatile boolean stepPause;
    protected volatile boolean pauseRequested;
    protected volatile boolean paused;
    protected volatile PauseStepType pauseStepType = None;
    protected volatile int stepToValue;
    protected volatile int stepToValue2;
    protected volatile Thread runningThread;

    protected Machine machine;
    protected Mapper mapper;
    protected CPU cpu;
    protected PPU ppu;
    protected APU apu;
    protected Register reg;

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
            this.reg = null;
        } else {
            this.machine = machine;
            this.mapper = machine.getMapper();
            this.cpu = machine.getCPU();
            this.ppu = machine.getPPU();
            this.apu = machine.getAPU();
            this.reg = machine.getCPU().getRegister();
        }
    }

    public Machine getMachine() {
        return machine;
    }

    public Mapper getMapper() {
        return mapper;
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
                    //rewind();
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
        App.setMachineRunner(this);
        App.updateFrames(this);
        final ImagePane imagePane = App.getImageFrame().getImagePane();
        imagePane.setRewinding(false);
        imagePane.setTVSystem(mapper.getTVSystem());

        long next = System.nanoTime();
        while (running) {

            suppressScreensaver();

            while (ppu.frameRendering) {

                if (pauseRequested) {
                    next = handlePause(next);
                }
                cpu.executeInstruction();
            }
            ppu.frameRendering = true;

            mapper.handleFrameRendered();

            App.handleFrameRendered(this);

            next = sleep(next, mapper);

            InputUtil.pollControllers(machine);
            controllerInput.input = InputUtil.getButtons();
            controllerInput.otherInputs = InputUtil.getOtherInputs();

            mapper.updateButtons(controllerInput.input);
            if (controllerInput.otherInputs != null) {
                for (final OtherInput otherInput : controllerInput.otherInputs) {
                    otherInput.run(machine);
                }
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
                    return cpu.getInstructionsCounter() == stepToValue;
                case Out:
                    switch (mapper.peekCpuMemory(reg.pc())) {
                        case PLP:
                        case PLA: {
                            final int stackSize = 0xFF - reg.sp() - 1;
                            if (stackSize < stepToValue) {
                                stepToValue = stackSize;
                            }
                            break;
                        }
                        case RTI:
                        case RTS:
                            if (0xFF - reg.sp() - 2 < stepToValue) {
                                stepToValue = cpu.getInstructionsCounter() + 1;
                                pauseStepType = Into;
                            }
                            break;
                    }
                    return false;
                case Address:
                    return reg.pc() == stepToValue;
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
                        return mapper.peekCpuMemory(reg.pc()) == stepToValue;
                    }
                case NMI:
                    if (stepToValue == 1) {
                        stepToValue = cpu.getServiced() == ServicedType.NMI ? 1 : 0;
                        break;
                    } else {
                        return cpu.getServiced() == ServicedType.NMI;
                    }
                case IRQ:
                    if (stepToValue == 1) {
                        stepToValue = cpu.getServiced() == ServicedType.IRQ ? 1 : 0;
                        break;
                    } else {
                        return cpu.getServiced() == ServicedType.IRQ;
                    }
                case BRK:
                    if (stepToValue == 1) {
                        stepToValue = cpu.getServiced() == ServicedType.BRK ? 1 : 0;
                        break;
                    } else {
                        return cpu.getServiced() == ServicedType.BRK;
                    }
                case RST:
                    if (stepToValue == 1) {
                        stepToValue = cpu.getServiced() == ServicedType.RST ? 1 : 0;
                        break;
                    } else {
                        return cpu.getServiced() == ServicedType.RST;
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
                    stepToValue = cpu.getInstructionsCounter() + 1;
                    break;
                case Out: {
                    final int opCode = mapper.peekCpuMemory(reg.pc());
                    if (opCode == RTI || opCode == RTS) {
                        pauseStepType = Into;
                        stepToValue = cpu.getInstructionsCounter() + 1;
                    } else {
                        stepToValue = 0xFF - reg.sp();
                    }
                    break;
                }
                case Over: {
                    final int opCode = mapper.peekCpuMemory(reg.pc());
                    if (opCode == JSR) {
                        pauseStepType = Address;
                        stepToValue = (reg.pc() + 3) & 0xFFFF;
                    } else {
                        pauseStepType = Into;
                        stepToValue = cpu.getInstructionsCounter() + 1;
                    }
                    break;
                }
                case Sprite0:
                    stepToValue = ppu.getFrameCounter() + (ppu.isSprite0Hit() ? 1 : 0);
                    break;
                case NMI:
                    stepToValue = cpu.getServiced() == ServicedType.NMI ? 1 : 0;
                    break;
                case IRQ:
                    stepToValue = cpu.getServiced() == ServicedType.IRQ ? 1 : 0;
                    break;
                case BRK:
                    stepToValue = cpu.getServiced() == ServicedType.BRK ? 1 : 0;
                    break;
                case RST:
                    stepToValue = cpu.getServiced() == ServicedType.RST ? 1 : 0;
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
            stepToValue2 = mapper.peekCpuMemory(reg.pc()) == opcode ? 1 : 0;
            step(Opcode);
        }
    }

    public synchronized void stepToInstructions(final int instructions) {
        if (paused) {
            stepToValue = cpu.getInstructionsCounter() + instructions;
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

    public void dispose() {
        cancel();
        waitForTermination();
    }

    @Override
    public void cancel() {
        if (running) {
            kill();
        }
    }
}
