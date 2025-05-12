package cn.kinlon.emu.mappers.thq;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class CPROM extends Mapper {

    private static final long serialVersionUID = 0;

    public CPROM(final CartFile cartFile) {
        super(cartFile, 2, 2);
        xram = new int[0x4000];
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return xram[chrBanks[address >> 12] | (address & 0x0FFF)];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            xram[chrBanks[address >> 12] | (address & 0x0FFF)] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank(1, value & 3);
    }
}
