package cn.kinlon.emu.mappers.kaiser;

// TODO ENHANCE WITH FDS AUDIO

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Kaiser7037 extends Mapper {

    private static final long serialVersionUID = 0;

    private int register;

    public Kaiser7037(final CartFile cartFile) {
        super(cartFile, 16, 0, 0x8000, 0x7000);
    }

    @Override
    public void init() {
        setPrgBank(7, 15);
        setPrgBank(10, -4);
        setPrgBank(14, -2);
        setPrgBank(15, -1);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0xB000) {
            return memory[0x7000 | (address & 0x0FFF)];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF000) == 0xB000) {
            memory[0x7000 | (address & 0x0FFF)] = value;
        } else {
            super.writeMemory(address, value);
        }
    }

    @Override
    protected void writeRegister(final int address, int value) {
        switch (address & 0xE001) {
            case 0x8000:
                register = value & 7;
                break;
            case 0x8001:
                switch (register) {
                    case 2:
                        setNametable(0, value & 1);
                        break;
                    case 3:
                        setNametable(2, value & 1);
                        break;
                    case 4:
                        setNametable(1, value & 1);
                        break;
                    case 5:
                        setNametable(3, value & 1);
                        break;
                    case 6:
                        value <<= 1;
                        setPrgBank(8, value);
                        setPrgBank(9, value | 1);
                        break;
                    case 7:
                        value <<= 1;
                        setPrgBank(12, value);
                        setPrgBank(13, value | 1);
                        break;
                }
                break;
        }
    }
}