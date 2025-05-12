package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.*;

public class BMC8In1 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int reg;

    public BMC8In1(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, ((reg & 0x0C) << 5) | (value & 0x7F));
    }

    @Override
    protected void setPrgBank(final int bank, int value) {
        if (getBitBool(reg, 4)) {
            super.setPrgBank(bank, ((reg & 0x0C) << 2) | (value & 0x0F));
        } else {
            value = (reg & 0x0F) << 2;
            super.setPrgBank(4, value);
            super.setPrgBank(5, value | 1);
            super.setPrgBank(6, value | 2);
            super.setPrgBank(7, value | 3);
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if (getBitBool(address, 12)) {
            reg = value;
            updateBanks();
        } else {
            super.writeRegister(address, value);
        }
    }
}