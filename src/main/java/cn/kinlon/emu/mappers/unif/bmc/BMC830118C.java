package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class BMC830118C extends MMC3 {

    private static final long serialVersionUID = 0;

    private int reg;

    public BMC830118C(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        reg = 0;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, ((reg & 0x0C) << 5) | (value & 0x7F));
    }

    @Override
    public void setPrgBank(final int bank, final int value) {
        if ((reg & 0x0C) == 0x0C) {
            if (bank == 4) {
                super.setPrgBank(4, ((reg & 0x0C) << 2) | (value & 0x0F));
                super.setPrgBank(6, 0x32 | (value & 0x0F));
            } else if (bank == 5) {
                super.setPrgBank(5, ((reg & 0x0C) << 2) | (value & 0x0F));
                super.setPrgBank(7, 0x32 | (value & 0x0F));
            }
        } else {
            super.setPrgBank(bank, ((reg & 0x0C) << 2) | (value & 0x0F));
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xFF00) == 0x6800) {
            reg = value;
            updateBanks();
        }
        super.writeMemory(address, value);
    }
}