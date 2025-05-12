package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;

public class Mapper200 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper200(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = address & 7;
        setChrBank(0, bank);
        setPrgBank(2, bank);
        setPrgBank(3, bank);
        setNametableMirroring(getBit(address, 3));
    }
}
