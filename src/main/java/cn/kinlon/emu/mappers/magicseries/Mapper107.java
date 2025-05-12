package cn.kinlon.emu.mappers.magicseries;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.nintendo.GxROM;

public class Mapper107 extends GxROM {

    private static final long serialVersionUID = 0;

    public Mapper107(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected void writeRegister(int address, int value) {
        setPrgBank(value >> 1);
        setChrBank(value);
    }
}
