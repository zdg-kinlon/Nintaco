package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class Sachen3014 extends Mapper {

    private static final long serialVersionUID = 0;

    private int latch;

    public Sachen3014(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        latch = value >> 4;
        setChrBank(latch & 1);
    }

    @Override
    public int readMemory(final int address) {
        int value = super.readMemory(address);
        if ((address & 0xF000) == 0xE000) {
            value = (value & 0xF0) | latch;
        }
        return value;
    }
}
