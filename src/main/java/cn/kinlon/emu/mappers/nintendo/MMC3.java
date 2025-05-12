package cn.kinlon.emu.mappers.nintendo;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class MMC3 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int MC_ACC = 3;
    private static final int MMC3A = 4;

    protected final int[] R = {0, 2, 4, 5, 6, 7, -4, -3};

    protected boolean mmc3a;
    protected boolean mc_acc;
    protected boolean requestIrqOnA12Fall;
    protected boolean chrMode;
    protected boolean prgMode;
    protected boolean prgRamWritesEnabled;
    protected boolean prgRamChipEnabled;
    protected boolean irqEnabled;
    protected boolean irqReloadRequest;
    protected int irqCounter;
    protected int irqReloadValue;
    protected int irqResetDelay;
    protected int register;
    protected int prgBlockMask = -1;
    protected int prgBlockOffset;
    protected int chrBlockMask = -1;
    protected int chrBlockOffset;

    public MMC3(final CartFile cartFile) {
        super(cartFile, 8, 8);

        switch (cartFile.getSubmapperNumber()) {
            case MC_ACC:
                mc_acc = true;
                break;
            case MMC3A:
//      case MMC3B:
                mmc3a = true;
                break;
        }
    }

    @Override
    public void init() {
        updateBanks();
    }

    private void triggerIRQ() {
        if (mc_acc) {
            requestIrqOnA12Fall = true;
        } else {
            cpu.setMapperIrq(true);
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (irqResetDelay > 0) {
            irqResetDelay--;
        }

        final boolean a12 = (address & 0x1000) != 0;
        if (a12) {
            if (irqResetDelay == 0) {
                boolean decrementedToZero = false;
                if (irqCounter > 0) {
                    irqCounter--;
                    decrementedToZero = irqCounter == 0;
                } else {
                    irqCounter = irqReloadValue;
                }
                if (irqReloadRequest) {
                    irqReloadRequest = false;
                    irqCounter = irqReloadValue;
                    decrementedToZero = irqCounter == 0;
                }
                if (mmc3a) {
                    if (decrementedToZero && irqEnabled) {
                        triggerIRQ();
                    }
                } else if (irqCounter == 0 && irqEnabled) {
                    triggerIRQ();
                }
            }
            irqResetDelay = mc_acc ? 35 : 8;
        } else if (requestIrqOnA12Fall) {
            requestIrqOnA12Fall = false;
            cpu.setMapperIrq(true);
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {
        switch (address & 0xE001) {
            case 0x8000:
                writeBankSelect(value);
                break;
            case 0x8001:
                writeBankData(value);
                break;
            case 0xA000:
                writeMirroring(value);
                break;
            case 0xA001:
                writePrgRamProtect(value);
                break;
            case 0xC000:
                writeIrqLatch(value);
                break;
            case 0xC001:
                writeIrqReload();
                break;
            case 0xE000:
                writeIrqDisable();
                break;
            case 0xE001:
                writeIrqEnable();
                break;
        }
    }

    protected void updateBanks() {
        updateChrBanks();
        updatePrgBanks();
    }

    protected void updateChrBanks() {
        if (chrMode) {
            setChrBank(0, R[2]);
            setChrBank(1, R[3]);
            setChrBank(2, R[4]);
            setChrBank(3, R[5]);
            setChrBank(4, R[0] & 0xFE);
            setChrBank(5, R[0] | 0x01);
            setChrBank(6, R[1] & 0xFE);
            setChrBank(7, R[1] | 0x01);
        } else {
            setChrBank(0, R[0] & 0xFE);
            setChrBank(1, R[0] | 0x01);
            setChrBank(2, R[1] & 0xFE);
            setChrBank(3, R[1] | 0x01);
            setChrBank(4, R[2]);
            setChrBank(5, R[3]);
            setChrBank(6, R[4]);
            setChrBank(7, R[5]);
        }
    }

    protected void updatePrgBanks() {
        if (prgMode) {
            setPrgBank(4, -2);
            setPrgBank(5, R[7]);
            setPrgBank(6, R[6]);
            setPrgBank(7, -1);
        } else {
            setPrgBank(4, R[6]);
            setPrgBank(5, R[7]);
            setPrgBank(6, -2);
            setPrgBank(7, -1);
        }
    }

    protected void writeBankSelect(final int value) {
        chrMode = getBitBool(value, 7);
        prgMode = getBitBool(value, 6);
        register = value & 7;
        updateBanks();
    }

    protected void writeBankData(final int value) {
        R[register] = value;
        if (register < 6) {
            updateChrBanks();
        } else {
            updatePrgBanks();
        }
    }

    protected void writeMirroring(final int value) {
        if (nametableMirroring < 2) {
            setNametableMirroring(value & 1);
        }
    }

    protected void writePrgRamProtect(final int value) {
        prgRamChipEnabled = getBitBool(value, 7);
        prgRamWritesEnabled = !getBitBool(value, 6);
    }

    protected void writeIrqLatch(final int value) {
        irqReloadValue = value;
    }

    protected void writeIrqReload() {
        irqCounter = 0;
        irqReloadRequest = true;
    }

    protected void writeIrqDisable() {
        irqEnabled = false;
        cpu.setMapperIrq(false);
    }

    protected void writeIrqEnable() {
        irqEnabled = true;
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, prgBlockOffset | (prgBlockMask & value));
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, chrBlockOffset | (chrBlockMask & value));
    }

    protected void setPrgBlockMask(final int prgBlockMask) {
        this.prgBlockMask = prgBlockMask;
        updatePrgBanks();
    }

    protected void setPrgBlockOffset(final int prgBlockOffset) {
        this.prgBlockOffset = prgBlockOffset;
        updatePrgBanks();
    }

    protected void setPrgBlock(final int prgBlockOffset, final int prgBlockMask) {
        this.prgBlockOffset = prgBlockOffset;
        this.prgBlockMask = prgBlockMask;
        updatePrgBanks();
    }

    public void setChrBlockMask(final int chrBlockMask) {
        this.chrBlockMask = chrBlockMask;
        updateChrBanks();
    }

    protected void setChrBlockOffset(final int chrBlockOffset) {
        this.chrBlockOffset = chrBlockOffset;
        updateChrBanks();
    }

    protected void setChrBlock(final int chrBlockOffset, final int chrBlockMask) {
        this.chrBlockOffset = chrBlockOffset;
        this.chrBlockMask = chrBlockMask;
        updateChrBanks();
    }

    protected void setBlock(final int prgBlockOffset, final int prgBlockMask,
                            final int chrBlockOffset, final int chrBlockMask) {
        this.prgBlockOffset = prgBlockOffset;
        this.prgBlockMask = prgBlockMask;
        this.chrBlockOffset = chrBlockOffset;
        this.chrBlockMask = chrBlockMask;
        updateBanks();
    }
}