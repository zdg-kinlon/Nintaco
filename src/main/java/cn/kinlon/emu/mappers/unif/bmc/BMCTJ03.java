package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCTJ03 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMCTJ03(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = (address >> 8) & 7;
        setPrgBank(bank);
        setChrBank(bank);
        setNametableMirroring(getBit(address, 1));
    }
}
