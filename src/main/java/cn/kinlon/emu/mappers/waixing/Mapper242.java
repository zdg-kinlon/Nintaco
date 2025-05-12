package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.rare.AxROM;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper242 extends AxROM {

    private static final long serialVersionUID = 0;

    public Mapper242(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void writeRegister(int address, int value) {
        prgBanks[1] = (address & 0x0078) << 12;
        setNametableMirroring(getBit(address, 1));
    }
}
