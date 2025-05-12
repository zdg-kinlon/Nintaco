package cn.kinlon.emu.mappers.frontfareast;

import cn.kinlon.emu.files.NesFile;

public class Mapper006 extends FrontFareast {

    private static final long serialVersionUID = 0;

    public Mapper006(final NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void init() {
        super.init();
        setPrgBanks(4, 2, 0);
        setPrgBanks(6, 2, 14);
    }

    @Override
    protected void writeRegister(final int address, int value) {
        if (chrRamPresent || ffeAltMode) {
            setPrgBanks(4, 2, (value & 0xFC) >> 1);
            value &= 0x03;
        }
        setChrBanks(0, 8, value << 3);
    }
}
