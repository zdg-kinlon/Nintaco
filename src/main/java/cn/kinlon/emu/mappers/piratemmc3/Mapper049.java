package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;

import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper049 extends BlockMMC3 {

    private static final long serialVersionUID = 0;

    protected int prgReg;
    protected boolean mmc3PrgMode;

    public Mapper049(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        sramRegister = false;
        super.init();
    }

    @Override
    protected void updateBlock(final int value) {
        setBlock((value & 0xC0) >> 2, 0x0F, (value & 0xC0) << 1, 0x7F);
        prgReg = (value & 0x30) << 11;
        mmc3PrgMode = getBitBool(value, 0);
        updatePrgBanks();
    }

    @Override
    protected void writePrgRamProtect(final int value) {
        super.writePrgRamProtect(value);
        sramRegister = prgRamWritesEnabled;
    }

    @Override
    protected void updatePrgBanks() {
        if (mmc3PrgMode) {
            super.updatePrgBanks();
        } else {
            setBanks(prgBanks, 4, prgReg, 4, 0x2000);
        }
    }
}
