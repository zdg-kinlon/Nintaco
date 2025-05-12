package cn.kinlon.emu.mappers.frontfareast;

import cn.kinlon.emu.files.NesFile;

public class Mapper008 extends FrontFareast {

    private static final long serialVersionUID = 0;

    public Mapper008(final NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void init() {
        super.init();
        setPrgBanks(4, 4, 0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBanks(4, 2, (value & 0xF8) >> 2);
        setChrBanks(0, 8, (value & 0x07) << 3);
    }
}
