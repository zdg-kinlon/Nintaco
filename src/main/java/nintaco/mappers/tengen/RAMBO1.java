package nintaco.mappers.tengen;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class RAMBO1 extends Mapper {

    private static final long serialVersionUID = 0;

    protected static final int RL = 16;
    protected static final int R0b = 17;
    protected static final int R1b = 18;

    protected static final int[][] PrgModes = {
            {6, 7, 15, RL},
            {15, 6, 7, RL},
    };

    protected static final int[][] ChrModes = {
            {0, R0b, 1, R1b, 2, 3, 4, 5},
            {0, 8, 1, 9, 2, 3, 4, 5},
            {2, 3, 4, 5, 0, R0b, 1, R1b},
            {2, 3, 4, 5, 0, 8, 1, 9},
    };

    protected int[] R = new int[19];
    protected int prgMode;
    protected int chrMode;
    protected int register;
    protected int cpuCycleCounter;
    protected int irqReloadValue;
    protected int irqCounter;
    protected int irqResetDelay;
    protected boolean irqReloadRequested;
    protected boolean irqCycleMode;
    protected boolean irqEnabled;
    protected boolean updateIrqCounter;

    public RAMBO1(final CartFile nesFile) {
        super(nesFile, 8, 8);
        R[RL] = -1;
    }

    @Override
    protected void writeRegister(int address, int value) {
        switch (address & 0xE001) {
            case 0x8000:
                writeModes(value);
                break;
            case 0x8001:
                writeRegister(value);
                break;
            case 0xA000:
                writeMirroring(value);
                break;
            case 0xC000:
                writeIrqLatch(value);
                break;
            case 0xC001:
                writeIrqModeSelectAndReload(value);
                break;
            case 0xE000:
                writeIrqAcknowledge();
                break;
            case 0xE001:
                writeIrqEnabled();
                break;
        }
    }

    protected void writeRegister(int value) {
        R[register] = value;
        if (register < 2) {
            R[register + R0b] = R[register] + 1;
        }
        updateBanks();
    }

    protected void writeMirroring(int value) {
        setNametableMirroring(value & 1);
    }

    protected void writeModes(int value) {
        register = value & 0x0F;
        prgMode = getBit(value, 6);
        chrMode = (getBit(value, 7) << 1) | getBit(value, 5);
        updateBanks();
    }

    protected void updateBanks() {
        updatePrgBanks();
        updateChrBanks();
    }

    protected void updatePrgBanks() {
        for (int i = 3; i >= 0; i--) {
            setPrgBank(i + 4, R[PrgModes[prgMode][i]]);
        }
    }

    protected void updateChrBanks() {
        for (int i = 7; i >= 0; i--) {
            setChrBank(i, R[ChrModes[chrMode][i]]);
        }
    }

    protected void writeIrqLatch(int value) {
        irqReloadValue = value;
    }

    protected void writeIrqModeSelectAndReload(int value) {
        irqCycleMode = getBitBool(value, 0);
        irqReloadRequested = true;
    }

    protected void writeIrqAcknowledge() {
        cpu.interrupt().setMapperIrq(false);
        irqEnabled = false;
    }

    protected void writeIrqEnabled() {
        irqEnabled = true;
    }

    protected void handleHorizontalBlank() {
        if (irqCounter == 0 || irqReloadRequested) {
            irqCounter = irqReloadValue;
            if (irqReloadRequested) {
                irqReloadRequested = false;
                irqCounter++;
            }
        } else if (--irqCounter == 0) {
            if (irqEnabled) {
                cpu.interrupt().setMapperIrq(true);
            }
        }
    }

    @Override
    public void update() {
        if (irqCycleMode && ++cpuCycleCounter == 4) {
            cpuCycleCounter = 0;
            handleHorizontalBlank();
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {
        if (!irqCycleMode && rendering && scanlineCycle == 264) {
            handleHorizontalBlank();
        }
    }
}