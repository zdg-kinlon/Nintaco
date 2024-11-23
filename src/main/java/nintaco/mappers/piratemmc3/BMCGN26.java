package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class BMCGN26 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean mmc3Mode;
    private boolean openBus;
    private boolean dip;

    public BMCGN26(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        mmc3Mode = openBus = false;
        writeOuterBankAndMode(0);
    }

    @Override
    public void resetting() {
        init();
    }

    public void writeOuterBankAndMode(final int address) {
        setChrBlock((address & 3) << 7, 0xFF);
        int outer = (address & 3) - 1;
        if (outer < 0) {
            outer = 0;
        }
        mmc3Mode = !getBitBool(address, 2);
        if (getBitBool(address, 3)) {
            openBus = dip;
            dip ^= true;
        } else {
            openBus = false;
            setPrgBlock(outer << 4, 0x0F);
        }
    }

    @Override
    protected void updatePrgBanks() {
        if (mmc3Mode) {
            super.updatePrgBanks();
        } else {
            set4PrgBanks(4, R[6] & 0xFC);
        }
    }

    @Override
    public int readMemory(final int address) {
        return (openBus && address >= 0x8000) ? 0xFF
                : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (prgRamWritesEnabled && prgRamChipEnabled
                && (address & 0xE000) == 0x6000) {
            writeOuterBankAndMode(address);
        }
        super.writeMemory(address, value);
    }
}