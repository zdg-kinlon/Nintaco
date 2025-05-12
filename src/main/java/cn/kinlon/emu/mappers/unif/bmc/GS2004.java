package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class GS2004 extends Mapper {

    private static final long serialVersionUID = 0;

    public GS2004(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBank(3, 0x1F);
        setPrgBanks(4, 4, 0x1C);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBanks(4, 4, (value & 7) << 2);
    }
}
