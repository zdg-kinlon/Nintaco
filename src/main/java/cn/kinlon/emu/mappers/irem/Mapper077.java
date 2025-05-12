package cn.kinlon.emu.mappers.irem;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper077 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper077(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x0800) {
            return chrROM[chrBanks[0] | (address & 0x07FF)];
        } else {
            return vram[address];
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        chrBanks[0] = (value & 0xF0) << 7;
        setPrgBank(value & 0x0F);
    }
}
