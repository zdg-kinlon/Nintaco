package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCK3036 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMCK3036(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void writeRegister(final int address, int value) {
        final int outer = address & 0x00FF;
        if (getBitBool(address, 5)) {
            setPrgBank(2, outer);
            setPrgBank(3, outer);
        } else {
            setPrgBank(2, outer | value);
            setPrgBank(3, outer | 7);
        }
        setNametableMirroring(((address & 0x25) == 0x25) ? HORIZONTAL : VERTICAL);
    }
}