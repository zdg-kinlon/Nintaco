package cn.kinlon.emu.mappers.jaleco;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.nintendo.CNROM;

public class JF10 extends CNROM {

    private static final long serialVersionUID = 0;

    public JF10(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        memory[address] = value;
        if ((address & 0xE000) == 0x6000) {
            setChrBank(0, value);
        }
    }
}
