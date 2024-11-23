package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;
import static nintaco.mappers.NametableMirroring.*;

public class BMCL6IN1 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean mmc3Banking;
    private boolean mmc3Mirroring;
    private int mirroring;
    private int bank;

    public BMCL6IN1(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBankAndMode(0);
    }

    private void writeOuterBankAndMode(final int value) {
        bank = (value & 3) << 2;
        mmc3Banking = (value & 0x0C) != 0;
        setPrgBlock((value & 0xC0) >> 2, 0x0F);
        mmc3Mirroring = !getBitBool(value, 5);
        if (mmc3Mirroring) {
            writeMirroring(mirroring);
        } else {
            setNametableMirroring(getBitBool(value, 4) ? ONE_SCREEN_B : ONE_SCREEN_A);
        }
    }

    @Override
    protected void updatePrgBanks() {
        if (mmc3Banking) {
            super.updatePrgBanks();
        } else {
            set4PrgBanks(4, bank);
        }
    }

    @Override
    protected void writeMirroring(final int value) {
        mirroring = value;
        if (mmc3Mirroring) {
            super.writeMirroring(value);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        super.writeMemory(address, value);
        if (prgRamWritesEnabled && prgRamChipEnabled
                && ((address & 0xE000) == 0x6000)) {
            writeOuterBankAndMode(value);
        }
    }
}