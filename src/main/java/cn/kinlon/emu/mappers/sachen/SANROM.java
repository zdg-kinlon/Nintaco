package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.NROM;



public class SANROM extends NROM {

    private static final long serialVersionUID = 0;

    public SANROM(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE100) == 0x4100) {
            return (~address) & 0x3F;
        } else {
            return super.readMemory(address);
        }
    }
}
