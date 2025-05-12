package cn.kinlon.emu.mappers.txc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class Mapper241 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper241(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x5FF0) {
            return 0xFF;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(value);
    }
}