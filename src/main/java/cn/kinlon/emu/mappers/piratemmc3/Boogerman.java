package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.*;

public class Boogerman extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] SECURITY = {0, 2, 5, 3, 6, 1, 7, 4};

    private final int[] regs = new int[4];

    private int bankSelect;

    public Boogerman(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        regs[0] = 0x00;
        regs[1] = 0xFF;
        regs[2] = 0x04;
        regs[3] = 0x00;
        super.updateBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address) {
            case 0x5000:
            case 0x6000:
                writePrg(value);
                break;
            case 0x5001:
            case 0x6001:
                writeChr(value);
                break;
            case 0x5007:
            case 0x6007:
                writeBanks(value);
                break;
            default:
                if ((address & 0xE001) == 0xE001) {
                    writeIrqEnable(value);
                } else if (address >= 0x8000) {
                    super.writeRegister(address, value);
                }
                break;
        }
    }

    @Override
    public void setPrgBank(final int bank, int value) {
        if (!getBitBool(regs[0], 7)) {
            if (getBitBool(regs[1], 3)) {
                value = (value & 0x1F) | 0x20;
            } else {
                value = (value & 0x0F) | (regs[1] & 0x10);
            }
        }
        super.setPrgBank(bank, value);
    }

    @Override
    public void setChrBank(final int bank, int value) {
        if (getBitBool(regs[1], 2)) {
            value |= 0x100;
        } else {
            value = (value & 0x7F) | ((regs[1] << 3) & 0x80);
        }
        super.setChrBank(bank, value);
    }

    @Override
    public void updatePrgBanks() {
        if (getBitBool(regs[0], 7)) {
            final int B = ((regs[0] & 0x0F) | (regs[1] & 0x10)) << 1;
            setPrgBank(4, B);
            setPrgBank(5, B | 1);
            setPrgBank(6, B);
            setPrgBank(7, B | 1);
        } else {
            super.updatePrgBanks();
        }
    }

    protected void writePrg(final int value) {
        if (regs[0] != value) {
            regs[0] = value;
            updatePrgBanks();
        }
    }

    protected void writeChr(final int value) {
        if (regs[1] != value) {
            regs[1] = value;
            super.updateChrBanks();
        }
    }

    protected void writeBanks(final int value) {
        bankSelect = 0;
        chrMode = false;
        prgMode = false;
        register = 0;
        if (regs[2] != value) {
            regs[2] = value;
            updatePrgBanks();
            super.updateChrBanks();
        }
    }

    @Override
    protected void writeBankSelect(final int value) {
        bankSelect = value;
        if (regs[2] == 0) {
            super.writeBankSelect(value);
        }
    }

    @Override
    protected void writeBankData(final int value) {
        if (regs[2] != 0) {
            if (regs[3] != 0 && (!getBitBool(regs[0], 7)
                    || (bankSelect & 7) < 6)) {
                regs[3] = 0;
                super.writeBankData(value);
            }
        } else {
            super.writeBankData(value);
        }
    }

    @Override
    protected void writeMirroring(int value) {
        if (regs[2] != 0) {
            value = (value & 0xC0) | SECURITY[value & 7];
            regs[3] = 1;
            super.writeBankSelect(value);
        } else {
            super.writeMirroring(value);
        }
    }

    @Override
    protected void writeIrqLatch(final int value) {
        if (regs[2] != 0) {
            super.writeMirroring((value >> 7) | value);
        } else {
            super.writeIrqLatch(value);
        }
    }

    @Override
    protected void writeIrqReload() {
        if (regs[2] != 0) {
            super.writeIrqEnable();
        } else {
            super.writeIrqReload();
        }
    }

    protected void writeIrqEnable(final int value) {
        if (regs[2] != 0) {
            super.writeIrqLatch(value);
            super.writeIrqReload();
        } else {
            super.writeIrqEnable();
        }
    }
}