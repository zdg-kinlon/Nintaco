package cn.kinlon.emu.mappers.carson;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Carson extends MMC3 {

    private static final long serialVersionUID = 0;

    private int prgRegister;
    private int copyProtection;

    public Carson(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void setPrgBank(final int bank, final int value) {
        if (getBitBool(prgRegister, 7)) {
            if (getBitBool(prgRegister, 5)) {
                final int B = (prgRegister & 0x0E) << 1;
                super.setPrgBank(4, B);
                super.setPrgBank(5, B | 1);
                super.setPrgBank(6, B | 2);
                super.setPrgBank(7, B | 3);
            } else {
                final int B = (prgRegister & 0x0F) << 1;
                super.setPrgBank(4, B);
                super.setPrgBank(5, B | 1);
                super.setPrgBank(6, B);
                super.setPrgBank(7, B | 1);
            }
        } else {
            super.setPrgBank(bank, value);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x8000) {
            writeRegister(address, value);
        } else if (address >= 0x4100) {
            switch (address) {
                case 0x5080:
                    copyProtection = value;
                    break;
                case 0x6000:
                    prgRegister = value;
                    break;
                case 0x6001:
                    setChrBlockOffset((value & 1) << 8);
                    break;
            }
            updatePrgBanks();
        }
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            return copyProtection;
        } else {
            return super.readMemory(address);
        }
    }
}