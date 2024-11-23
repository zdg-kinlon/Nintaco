package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper121 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int LUT[] = {0x83, 0x83, 0x42, 0x00};

    private final int[] regs = new int[8];

    public Mapper121(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        regs[0] = regs[1] = regs[2] = 0;
        regs[3] = 0x80;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            return regs[4];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        final int v = (regs[3] & 0x80) >> 2;
        if ((regs[5] & 0x3F) != 0) {
            super.setPrgBank(bank, (value & 0x1F) | v);
            super.setPrgBank(5, regs[2] | v);
            super.setPrgBank(6, regs[1] | v);
            super.setPrgBank(7, regs[0] | v);
        } else {
            super.setPrgBank(bank, (value & 0x1F) | v);
        }
    }

    @Override
    protected void setChrBank(final int bank, int value) {
        if (prgRomLength == chrRomLength) {
            super.setChrBank(bank, value | ((regs[3] & 0x80) << 1));
        } else {
            if ((bank >= 4) == chrMode) {
                value |= 0x100;
            }
            super.setChrBank(bank, value);
        }
    }

    private void updateRegisters() {
        switch (regs[5] & 0x3F) {
            case 0x20:
                regs[7] = 1;
                regs[0] = regs[6];
                break;
            case 0x29:
                regs[7] = 1;
                regs[0] = regs[6];
                break;
            case 0x26:
                regs[7] = 0;
                regs[0] = regs[6];
                break;
            case 0x2B:
                regs[7] = 1;
                regs[0] = regs[6];
                break;
            case 0x2C:
                regs[7] = 1;
                if (regs[6] != 0) {
                    regs[0] = regs[6];
                }
                break;
            case 0x3C:
            case 0x3F:
                regs[7] = 1;
                regs[0] = regs[6];
                break;
            case 0x28:
                regs[7] = 0;
                regs[1] = regs[6];
                break;
            case 0x2A:
                regs[7] = 0;
                regs[2] = regs[6];
                break;
            case 0x2F:
                break;
            default:
                regs[5] = 0;
                break;
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xF000) {
            case 0x5000:
                regs[4] = LUT[value & 0x03];
                if ((address & 0x5180) == 0x5180) {
                    regs[3] = value;
                    updateBanks();
                }
                break;
            case 0x8000:
            case 0x9000:
                if ((address & 0x03) == 0x03) {
                    regs[5] = value;
                    updateRegisters();
                    writeBankSelect(value);
                } else if (getBitBool(address, 0)) {
                    regs[6] = ((value & 0x01) << 5) | ((value & 0x02) << 3)
                            | ((value & 0x04) << 1) | ((value & 0x08) >> 1)
                            | ((value & 0x10) >> 3) | ((value & 0x20) >> 5);
                    if (regs[7] == 0) {
                        updateRegisters();
                    }
                    writeBankData(value);
                } else {
                    writeBankSelect(value);
                }
                break;
            default:
                if (address >= minRegisterAddress) {
                    writeRegister(address, value);
                }
                break;
        }
    }
}