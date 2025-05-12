package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class SA72008 extends Mapper {

    private static final long serialVersionUID = 0;

    public SA72008(final CartFile cartFile) {
        super(cartFile, 2, 1, 0x4100, 0x8000);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0x6100) == 0x4100) {
            setChrBank(value & 3);
            setPrgBank(getBit(value, 2));
        }
    }
}
