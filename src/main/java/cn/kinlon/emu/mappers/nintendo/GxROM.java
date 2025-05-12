package cn.kinlon.emu.mappers.nintendo;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class GxROM extends Mapper {

    private static final long serialVersionUID = 0;

    public GxROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank((value & 0x30) >> 4);
        setChrBank(value & 0x03);
    }
}

