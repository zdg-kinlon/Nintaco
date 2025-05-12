package cn.kinlon.emu.mappers.unif.bmc;

// TODO DIP SWITCHES?

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCSA005A extends Mapper {

    private static final long serialVersionUID = 0;

    public BMCSA005A(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = address & 0x0F;
        setChrBank(0, bank);
        setPrgBank(2, bank);
        setPrgBank(3, bank);
        setNametableMirroring(getBit(address, 3) ^ 1);
    }
}
