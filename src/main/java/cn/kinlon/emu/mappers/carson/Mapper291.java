package cn.kinlon.emu.mappers.carson;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper291 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean mmc3Mode;

    public Mapper291(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBank(0);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeOuterBank(final int value) {
        mmc3Mode = !getBitBool(value, 5);
        if (mmc3Mode) {
            setPrgBlock((value & 0x40) >> 2, 0x0F);
        } else {
            setPrgBlock(0, -1);
            set4PrgBanks(4, (((value & 0x40) >> 3) | (value & 0x06)) << 1);
        }
        setChrBlock((value & 0x40) << 2, 0xFF);
    }

    @Override
    public void updatePrgBanks() {
        if (mmc3Mode) {
            super.updatePrgBanks();
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            writeOuterBank(value);
        }
        super.writeMemory(address, value);
    }
}