package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
public class Mapper044 extends BlockMMC3 {

    private static final long serialVersionUID = 0;

    public Mapper044(final CartFile cartFile) {
        super(cartFile);
        sramRegister = false;
    }

    @Override
    protected void writePrgRamProtect(final int value) {
        super.writePrgRamProtect(value);
        updateBlock(value);
    }

    @Override
    protected void updateBlock(int value) {
        int block = value & 7;
        if (block == 7) {
            block = 6;
        }
        setBlock(block << 4, block == 6 ? 0x1F : 0x0F,
                block << 7, block == 6 ? 0xFF : 0x7F);
    }
}
