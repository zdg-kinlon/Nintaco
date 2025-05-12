package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper120 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper120(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBank(3, 0);
        setPrgBanks(4, 4, 8);
        setChrBank(0);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address == 0x41FF) {
            setPrgBank(3, value);
        }
    }
}