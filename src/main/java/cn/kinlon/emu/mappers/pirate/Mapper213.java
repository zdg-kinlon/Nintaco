package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper213 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper213(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank((address >> 3) & 7);
        setPrgBank((address >> 1) & 3);
    }
}