package cn.kinlon.emu.mappers.bandai;

import cn.kinlon.emu.files.NesFile;

public class Mapper153 extends FCG {

    private static final long serialVersionUID = 0;

    public Mapper153(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (address >= 0x8000) {
            if ((address & 0xFFFC) == 0x8000) {
                prgBanks[2] = (prgBanks[2] & 0x3C000) | ((value & 1) << 18);
            } else {
                super.writeRegister(address, value);
            }
        }
    }

}
