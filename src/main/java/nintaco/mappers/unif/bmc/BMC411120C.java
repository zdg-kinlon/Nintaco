package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class BMC411120C extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean mmc3Mode;
    private boolean dip;

    public BMC411120C(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBank(0);
        super.init();
    }

    @Override
    public void resetting() {
        dip ^= true;
        init();
    }

    private void writeOuterBank(final int value) {
        mmc3Mode = (value & ((dip && prgRomLength <= 0x8000) ? 0x0C : 0x08)) == 0;
        final int outer = value & 7;
        if (mmc3Mode) {
            setPrgBlock(outer << 4, 0x0F);
        } else {
            setPrgBlock(0, -1);
            set4PrgBanks(4, ((outer << 2) | ((value >> 4) & 3)) << 2);
        }
        setChrBlock(outer << 7, 0x7F);
        updateBanks();
    }

    @Override
    public void updatePrgBanks() {
        if (mmc3Mode) {
            super.updatePrgBanks();
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (prgRamWritesEnabled && prgRamChipEnabled
                && (address & 0xE000) == 0x6000) {
            writeOuterBank(value);
        }
        super.writeMemory(address, value);
    }
}