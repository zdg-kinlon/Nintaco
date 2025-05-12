package cn.kinlon.emu.mappers.bandai;

import cn.kinlon.emu.files.NesFile;

public class Mapper159 extends FCG {

    private static final long serialVersionUID = 0;

    public Mapper159(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void init() {
        x24c02 = 0;
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (address >= 0x8000) {
            int offset = address & 0x000F;
            if (offset < 8) {
                setChrBank(offset, value);
            } else {
                super.writeRegister(address, value);
            }
        }
    }
}
