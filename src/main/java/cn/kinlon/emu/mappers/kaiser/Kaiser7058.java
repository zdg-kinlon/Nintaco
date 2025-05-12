package cn.kinlon.emu.mappers.kaiser;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Kaiser7058 extends Mapper {

    private static final long serialVersionUID = 0;

    public Kaiser7058(final CartFile cartFile) {
        super(cartFile, 2, 2, 0xF000, 0x8000);
    }

    @Override
    public void init() {
        setPrgBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF080) {
            case 0xF000:
                setChrBank(0, value);
                break;
            case 0xF080:
                setChrBank(1, value);
                break;
        }
    }
}
