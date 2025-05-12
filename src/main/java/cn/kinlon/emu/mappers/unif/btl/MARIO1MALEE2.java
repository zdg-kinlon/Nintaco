package cn.kinlon.emu.mappers.unif.btl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class MARIO1MALEE2 extends Mapper {

    private static final long serialVersionUID = 0;

    public MARIO1MALEE2(final CartFile cartFile) {
        super(cartFile, 0, 1);
    }

    @Override
    public void init() {
        System.arraycopy(prgROM, 0x8000, memory, 0x6000, 0x0800);
        System.arraycopy(prgROM, 0x8000, memory, 0x6800, 0x0800);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x7000) {
            return memory[0x7000 | (address & 0x07FF)];
        } else {
            return super.readMemory(address & 0xFFFF);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF000) == 0x7000) {
            memory[0x7000 | (address & 0x07FF)] = value;
        } else {
            super.writeMemory(address & 0xFFFF, value);
        }
    }
}