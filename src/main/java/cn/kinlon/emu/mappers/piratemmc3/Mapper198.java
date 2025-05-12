package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class Mapper198 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper198(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void setPrgBank(final int bank, final int value) {
        if (value >= 0x50) {
            super.setPrgBank(bank, value & 0x4F);
        } else {
            super.setPrgBank(bank, value);
        }
    }
}
