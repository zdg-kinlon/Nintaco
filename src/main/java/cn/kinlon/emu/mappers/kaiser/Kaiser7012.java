package cn.kinlon.emu.mappers.kaiser;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Kaiser7012 extends Mapper {

    private static final long serialVersionUID = 0;

    public Kaiser7012(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(1);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address) {
            case 0xE0A0:
                setPrgBank(0);
                break;
            case 0xEE36:
                setPrgBank(1);
                break;
        }
    }
}
