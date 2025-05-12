package cn.kinlon.emu.mappers.ce;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper244 extends Mapper {

    private static final int[][] prgLUT = {
            {0, 1, 2, 3},
            {3, 2, 1, 0},
            {0, 2, 1, 3},
            {3, 1, 2, 0}
    };

    private static final int[][] chrLUT = {
            {0, 1, 2, 3, 4, 5, 6, 7},
            {0, 2, 1, 3, 4, 6, 5, 7},
            {0, 1, 4, 5, 2, 3, 6, 7},
            {0, 4, 1, 5, 2, 6, 3, 7},
            {0, 4, 2, 6, 1, 5, 3, 7},
            {0, 2, 4, 6, 1, 3, 5, 7},
            {7, 6, 5, 4, 3, 2, 1, 0},
            {7, 6, 5, 4, 3, 2, 1, 0}
    };

    public Mapper244(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        super.init();
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (getBitBool(value, 3)) {
            setChrBank(chrLUT[(value >> 4) & 0x07][value & 0x07]);
        } else {
            setPrgBank(prgLUT[(value >> 4) & 0x03][value & 0x03]);
        }
    }
}
