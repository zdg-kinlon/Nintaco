package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper187 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] security = {0x83, 0x83, 0x42, 0x00};

    private final int[] regs = new int[2];

    public Mapper187(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        if ((bank >= 4) == chrMode) {
            super.setChrBank(bank, value | 0x100);
        } else {
            super.setChrBank(bank, value);
        }
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        if (getBitBool(regs[0], 7)) {
            final int b = regs[0] & 0x1F;
            if (getBitBool(regs[0], 5)) {
                final int B = getBitBool(regs[0], 6) ? (b & 0xFC) : ((b >> 1) << 2);
                super.setPrgBank(4, B);
                super.setPrgBank(5, B | 1);
                super.setPrgBank(6, B | 2);
                super.setPrgBank(7, B | 3);
            } else {
                final int B = b << 1;
                super.setPrgBank(4, B);
                super.setPrgBank(5, B | 1);
                super.setPrgBank(6, B);
                super.setPrgBank(7, B | 1);
            }
        } else {
            super.setPrgBank(bank, value & 0x3F);
        }
    }

    @Override
    protected void writeBankSelect(final int value) {
        regs[1] = 1;
        super.writeBankSelect(value);
    }

    @Override
    protected void writeBankData(final int value) {
        if (regs[1] != 0) {
            super.writeBankData(value);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x8000) {
            writeRegister(address, value);
        } else if (address == 0x5000 || address == 0x6000) {
            regs[0] = value;
            updatePrgBanks();
        }
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            return security[regs[1] & 3];
        } else {
            return super.readMemory(address);
        }
    }
}