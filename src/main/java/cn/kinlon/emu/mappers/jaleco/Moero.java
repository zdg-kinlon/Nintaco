package cn.kinlon.emu.mappers.jaleco;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.nintendo.GxROM;

public class Moero extends GxROM {

    private static final long serialVersionUID = 0;

    public Moero(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address < 0x6000) {
            memory[address] = value;
        } else if (address < 0x7000) {
            prgBanks[1] = (value & 0x30) << 11;
            setChrBank(((value & 0x40) >> 4) | (value & 0x03));
        }
    }
}
