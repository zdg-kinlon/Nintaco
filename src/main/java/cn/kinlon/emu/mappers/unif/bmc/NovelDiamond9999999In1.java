package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class NovelDiamond9999999In1 extends Mapper {

    private static final long serialVersionUID = 0;

    public NovelDiamond9999999In1(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(1, 0);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(1, address & 3);
        setChrBank(address & 7);
    }
}
