package cn.kinlon.emu.mappers.jy;

// TODO REALLY MC-ACC MMC3 ?

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

public class Mapper361 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper361(final CartFile cartFile) {
        super(cartFile);
        mc_acc = true;
    }

    @Override
    public void init() {
        writeOuterBankRegister(0);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeOuterBankRegister(int value) {
        value &= 0xF0;
        setPrgBlock(value, 0x0F);
        setChrBlock(value << 3, 0x7F);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF000) == 0x7000) {
            writeOuterBankRegister(value);
        }
        super.writeMemory(address, value);
    }
}