package cn.kinlon.emu.mappers.nanjing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper534 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper534(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeReg(0);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeReg(final int value) {
        if (getBitBool(value, 0)) {
            setPrgBlock(0x04, 0x03);
        } else {
            setPrgBlock(0x00, getBitBool(value, 6) ? 0x0F : 0x1F);
        }
        updatePrgBanks();
    }

    @Override
    public void updateChrBanks() {
        set8ChrBanks(0, 0);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (prgRamWritesEnabled && prgRamChipEnabled
                && (address & 0xE003) == 0x6000) {
            writeReg(value);
        }
        super.writeMemory(address, value);
    }
}