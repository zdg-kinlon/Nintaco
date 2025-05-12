package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class GS2013 extends Mapper {

    private static final long serialVersionUID = 0;

    public GS2013(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBank(3, 0x1F);
        writeRegister(0x8000, 0);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBanks(4, 4, (value & 0x0F) << 2);
    }
}