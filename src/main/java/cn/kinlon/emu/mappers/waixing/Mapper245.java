package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class Mapper245 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int chrModeMask;

    public Mapper245(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readVRAM(final int address) {
        if (chrRamPresent && address < 0x2000) {
            return vram[address ^ chrModeMask];
        } else {
            return super.readVRAM(address);
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (chrRamPresent && address < 0x2000) {
            vram[address ^ chrModeMask] = value;
        } else {
            super.writeVRAM(address, value);
        }
    }

    @Override
    protected void writeBankSelect(final int value) {
        super.writeBankSelect(value);
        chrModeMask = chrMode ? 0x1000 : 0x0000;
    }

    @Override
    protected void writeBankData(final int value) {
        if (register == 0) {
            setPrgBlock((value & 0x02) << 5, 0x3F);
        }
        super.writeBankData(value);
    }
}