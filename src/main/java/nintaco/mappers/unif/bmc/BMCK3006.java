package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class BMCK3006 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean mmc3Mode;

    public BMCK3006(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBankAndMode(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void updatePrgBanks() {
        if (mmc3Mode) {
            super.updatePrgBanks();
        }
    }

    private void writeOuterBankAndMode(final int address) {
        mmc3Mode = getBitBool(address, 5);
        if (mmc3Mode) {
            setPrgBlock((address & 0x18) << 1, 0x0F);
        } else {
            setPrgBlock(0, -1);
            if ((address & 7) == 6) {
                set4PrgBanks(4, (address & 0x1E) << 1);
            } else {
                set2PrgBanks(4, (address & 0x1F) << 1);
                set2PrgBanks(6, (address & 0x1F) << 1);
            }
        }
        setChrBlock((address & 0x18) << 4, 0x7F);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (prgRamWritesEnabled && prgRamChipEnabled
                && (0xE000 & address) == 0x6000) {
            writeOuterBankAndMode(address);
        }
        super.writeMemory(address, value);
    }
}