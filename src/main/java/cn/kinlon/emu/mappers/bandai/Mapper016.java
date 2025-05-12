package cn.kinlon.emu.mappers.bandai;

import cn.kinlon.emu.files.CartFile;

public class Mapper016 extends FCG {

    private static final long serialVersionUID = 0;

    public Mapper016(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        x24c02 = 1;
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        int offset = address & 0x000F;
        if (offset < 8) {
            setChrBank(offset, value);
        } else {
            super.writeRegister(address, value);
        }
    }

}
