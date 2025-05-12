package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class GKCX1 extends Mapper {

    private static final long serialVersionUID = 0;

    public GKCX1(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank((address >> 3) & 3);
        setChrBank(address & 7);
    }
}
