package cn.kinlon.emu.mappers.rare;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.*;

public class AxROM extends Mapper {

    private static final long serialVersionUID = 0;

    public AxROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(value & 0x0F);
        setNametableMirroring(ONE_SCREEN_A + getBit(value, 4));
    }
}
