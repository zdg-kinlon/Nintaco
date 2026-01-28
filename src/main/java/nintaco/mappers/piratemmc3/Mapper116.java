package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.*;

public class Mapper116 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] MIRRORING
            = {ONE_SCREEN_A, ONE_SCREEN_B, VERTICAL, HORIZONTAL};

    private static final int MapperModeVRC2b = 0;
    private static final int MapperModeMMC3 = 1;

    private final int[] vrc2Chr = {-1, -1, -1, -1, 4, 5, 6, 7};
    private final int[] vrc2Prg = {0, 1};
    private final int[] mmc3Regs = {0, 2, 4, 5, 6, 7, -4, -3, -2, -1};
    private final int[] mmc1Regs = {12, 0, 0, 0};

    private int mode;
    private int vrc2Mirror;
    private int mmc3Control;
    private int mmc3Mirror;
    private int mmc1Buffer;
    private int mmc1Shift;

    private int irqCounter;
    private int irqReloadValue;
    private int irqResetDelay;
    private boolean irqEnabled;
    private boolean irqReloadRequest;

    public Mapper116(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        updateBanks();
    }

    private void updatePrgBanks() {
        switch (mode & 3) {

            case MapperModeVRC2b:
                setPrgBank(4, vrc2Prg[0]);
                setPrgBank(5, vrc2Prg[1]);
                setPrgBank(6, -2);
                setPrgBank(7, -1);
                break;

            case MapperModeMMC3: {
                final int swap = (mmc3Control >> 5) & 0x02;
                setPrgBank(4, mmc3Regs[6 + swap]);
                setPrgBank(5, mmc3Regs[7]);
                setPrgBank(6, mmc3Regs[6 + (swap ^ 2)]);
                setPrgBank(7, mmc3Regs[9]);
                break;
            }

            default: {
                final int bank = mmc1Regs[3] & 0x0F;
                if (getBitBool(mmc1Regs[0], 3)) {
                    final int b = bank << 1;
                    if (getBitBool(mmc1Regs[0], 2)) {
                        setPrgBank(4, b);
                        setPrgBank(5, b | 1);
                        setPrgBank(6, 0x1E);
                        setPrgBank(7, 0x1F);
                    } else {
                        setPrgBank(4, 0);
                        setPrgBank(5, 1);
                        setPrgBank(6, b);
                        setPrgBank(7, b | 1);
                    }
                } else {
                    final int b = (bank >> 1) << 2;
                    setPrgBank(4, b);
                    setPrgBank(5, b | 1);
                    setPrgBank(6, b | 2);
                    setPrgBank(7, b | 3);
                }
                break;
            }
        }
    }

    private void updateChrBanks() {
        final int base = (mode & 4) << 6;
        switch (mode & 3) {

            case MapperModeVRC2b:
                setChrBank(0, base | vrc2Chr[0]);
                setChrBank(1, base | vrc2Chr[1]);
                setChrBank(2, base | vrc2Chr[2]);
                setChrBank(3, base | vrc2Chr[3]);
                setChrBank(4, base | vrc2Chr[4]);
                setChrBank(5, base | vrc2Chr[5]);
                setChrBank(6, base | vrc2Chr[6]);
                setChrBank(7, base | vrc2Chr[7]);
                break;

            case MapperModeMMC3:
                if (getBitBool(mmc3Control, 7)) {
                    setChrBank(0, base | mmc3Regs[2]);
                    setChrBank(1, base | mmc3Regs[3]);
                    setChrBank(2, base | mmc3Regs[4]);
                    setChrBank(3, base | mmc3Regs[5]);
                    setChrBank(4, base | mmc3Regs[0] & 0xFE);
                    setChrBank(5, base | mmc3Regs[0] | 0x01);
                    setChrBank(6, base | mmc3Regs[1] & 0xFE);
                    setChrBank(7, base | mmc3Regs[1] | 0x01);
                } else {
                    setChrBank(0, base | mmc3Regs[0] & 0xFE);
                    setChrBank(1, base | mmc3Regs[0] | 0x01);
                    setChrBank(2, base | mmc3Regs[1] & 0xFE);
                    setChrBank(3, base | mmc3Regs[1] | 0x01);
                    setChrBank(4, base | mmc3Regs[2]);
                    setChrBank(5, base | mmc3Regs[3]);
                    setChrBank(6, base | mmc3Regs[4]);
                    setChrBank(7, base | mmc3Regs[5]);
                }
                break;

            default:
                if (getBitBool(mmc1Regs[0], 4)) {
                    final int b1 = mmc1Regs[1] << 2;
                    setChrBank(0, b1);
                    setChrBank(1, b1 | 1);
                    setChrBank(2, b1 | 2);
                    setChrBank(3, b1 | 3);
                    final int b2 = mmc1Regs[2] << 2;
                    setChrBank(4, b2);
                    setChrBank(5, b2 | 1);
                    setChrBank(6, b2 | 2);
                    setChrBank(7, b2 | 3);
                } else {
                    final int b = (mmc1Regs[1] >> 1) << 3;
                    setChrBank(0, b);
                    setChrBank(1, b | 1);
                    setChrBank(2, b | 2);
                    setChrBank(3, b | 3);
                    setChrBank(4, b | 4);
                    setChrBank(5, b | 5);
                    setChrBank(6, b | 6);
                    setChrBank(7, b | 7);
                }
                break;
        }
    }

    private void updateNametableMirroring() {
        switch (mode & 3) {
            case MapperModeVRC2b:
                setNametableMirroring(vrc2Mirror & 1);
                break;
            case MapperModeMMC3:
                setNametableMirroring(mmc3Mirror & 1);
                break;
            default:
                setNametableMirroring(MIRRORING[mmc1Regs[0] & 3]);
                break;
        }
    }

    private void updateBanks() {
        updatePrgBanks();
        updateChrBanks();
        updateNametableMirroring();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x4100 && address <= 0x7FFF
                && (address & 0x4100) == 0x4100) {
            mode = value;
            if (getBitBool(address, 0)) {
                mmc1Regs[0] = 0x0c;
                mmc1Regs[3] = 0;
                mmc1Buffer = 0;
                mmc1Shift = 0;
            }
            updateBanks();
        }
        super.writeMemory(address, value);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (mode & 3) {
            case MapperModeVRC2b:
                if (address >= 0xB000 && address <= 0xE003) {
                    final int index = ((((address & 2) | (address >> 10)) >> 1) + 2) & 7;
                    final int shift = (address & 1) << 2;
                    vrc2Chr[index] = (vrc2Chr[index] & (0xF0 >> shift))
                            | ((value & 0x0F) << shift);
                    updateChrBanks();
                } else {
                    switch (address & 0xF000) {
                        case 0x8000:
                            vrc2Prg[0] = value;
                            updatePrgBanks();
                            break;
                        case 0xA000:
                            vrc2Prg[1] = value;
                            updatePrgBanks();
                            break;
                        case 0x9000:
                            vrc2Mirror = value;
                            updateNametableMirroring();
                            break;
                    }
                }
                break;

            case MapperModeMMC3:
                switch (address & 0xE001) {
                    case 0x8000: {
                        final int old_ctrl = mmc3Control;
                        mmc3Control = value;
                        if ((old_ctrl & 0x40) != (mmc3Control & 0x40)) {
                            updatePrgBanks();
                        }
                        if ((old_ctrl & 0x80) != (mmc3Control & 0x80)) {
                            updateChrBanks();
                        }
                        break;
                    }
                    case 0x8001:
                        mmc3Regs[mmc3Control & 7] = value;
                        if ((mmc3Control & 7) < 6) {
                            updateChrBanks();
                        } else {
                            updatePrgBanks();
                        }
                        break;
                    case 0xA000:
                        mmc3Mirror = value;
                        updateNametableMirroring();
                        break;
                    case 0xC000:
                        irqReloadValue = value;
                        break;
                    case 0xC001:
                        irqReloadRequest = true;
                        break;
                    case 0xE000:
                        cpu.interrupt().setMapperIrq(false);
                        irqEnabled = false;
                        break;
                    case 0xE001:
                        irqEnabled = true;
                        break;
                }
                break;

            default:
                if (getBitBool(value, 7)) {
                    mmc1Regs[0] |= 0x0c;
                    mmc1Buffer = mmc1Shift = 0;
                    updatePrgBanks();
                } else {
                    final int n = (address >> 13) - 4;
                    mmc1Buffer |= (value & 1) << (mmc1Shift++);
                    if (mmc1Shift == 5) {
                        mmc1Regs[n] = mmc1Buffer;
                        mmc1Buffer = mmc1Shift = 0;
                        switch (n) {
                            case 0:
                                updateNametableMirroring();
                            case 2:
                                updateChrBanks();
                            case 3:
                            case 1:
                                updatePrgBanks();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if ((mode & 3) == MapperModeMMC3) {
            if (irqResetDelay > 0) {
                irqResetDelay--;
            }

            final boolean a12 = (address & 0x1000) != 0;
            if (a12 && irqResetDelay == 0) {
                if (irqCounter > 0) {
                    irqCounter--;
                } else {
                    irqCounter = irqReloadValue;
                }
                if (irqReloadRequest) {
                    irqReloadRequest = false;
                    irqCounter = irqReloadValue;
                }
                if (irqCounter == 0 && irqEnabled) {
                    cpu.interrupt().setMapperIrq(true);
                }
            }
            if (a12) {
                irqResetDelay = 8;
            }
        }
    }
}