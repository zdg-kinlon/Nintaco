package cn.kinlon.emu.mappers.ave;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.GxROM;

public class NINA003006 extends GxROM {

    private static final long serialVersionUID = 0;

    public NINA003006(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        if ((address & 0xE100) == 0x4100) {
            setChrBank(value & 0x07);
            prgBanks[1] = (value & 0x08) << 12;
        } else {
            memory[address] = value;
        }
    }
}
