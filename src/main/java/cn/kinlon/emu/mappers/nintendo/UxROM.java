package cn.kinlon.emu.mappers.nintendo;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class UxROM extends Mapper {

    private static final long serialVersionUID = 0;

    public UxROM(final CartFile cartFile) {
        super(cartFile, 4, 0);
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(2, value);
    }
}