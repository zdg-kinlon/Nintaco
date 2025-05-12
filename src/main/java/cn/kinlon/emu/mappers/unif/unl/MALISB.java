package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class MALISB extends MMC3 {

    private static final long serialVersionUID = 0;

    public MALISB(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, (value & 3) | ((value & 8) >> 1)
                | ((value & 4) << 1));
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, (value & 0xDD) | ((value & 0x20) >> 4)
                | ((value & 0x02) << 4));
    }

    @Override
    public void writeRegister(int address, final int value) {
        if (address >= 0xC000) {
            address = (address & 0xFFFE) | ((address >> 2) & 1)
                    | ((address >> 3) & 1);
        } else {
            address = (address & 0xFFFE) | ((address >> 3) & 1);
        }
        super.writeRegister(address, value);
    }
}
