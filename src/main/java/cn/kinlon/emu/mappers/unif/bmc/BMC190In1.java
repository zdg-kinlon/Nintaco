package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class BMC190In1 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMC190In1(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = (value >> 2) & 7;
        setPrgBank(2, bank);
        setPrgBank(3, bank);
        setChrBank(bank);
        setNametableMirroring(value & 1);
    }
}
