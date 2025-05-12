package cn.kinlon.emu.mappers.namco;

import cn.kinlon.emu.files.CartFile;

public class Mapper088 extends DxROM {

    private static final long serialVersionUID = 0;

    public Mapper088(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected int readChr(final int address) {
        return chrROM[(((address & 0x1000) << 4)
                | chrBanks[address >> 10] | (address & 0x03FF)) & chrRomSizeMask];
    }

    @Override
    protected void writeChrBank(final int bank, final int value) {
        setChrBank(bank, value);
    }

    @Override
    protected void writeChrBank2K(final int bank, int value) {
        value &= 0xFE;
        setChrBank(bank, value);
        setChrBank(bank + 1, value | 1);
    }
}
