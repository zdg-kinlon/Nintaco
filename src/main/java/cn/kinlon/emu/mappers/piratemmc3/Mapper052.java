package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;


import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper052 extends BlockMMC3 {

    private static final long serialVersionUID = 0;

    public Mapper052(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void resetting() {
        sramRegister = true;
        init();
    }

    @Override
    protected void updateBlock(final int value) {

        sramRegister = !getBitBool(value, 7);

        final int prgOffset;
        final int prgMask;
        if (getBitBool(value, 3)) {
            prgOffset = (value & 0x07) << 4;
            prgMask = 0x0F;
        } else {
            prgOffset = (value & 0x06) << 4;
            prgMask = 0x1F;
        }

        final int chrOffset;
        final int chrMask;
        if (getBitBool(value, 6)) {
            chrOffset = ((value & 0x20) << 4) | ((value & 0x04) << 6)
                    | ((value & 0x10) << 3);
            chrMask = 0x7F;
        } else {
            chrOffset = ((value & 0x20) << 4) | ((value & 0x04) << 6);
            chrMask = 0xFF;
        }

        setBlock(prgOffset, prgMask, chrOffset, chrMask);
    }
}
