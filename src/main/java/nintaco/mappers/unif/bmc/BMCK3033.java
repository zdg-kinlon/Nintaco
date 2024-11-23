package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class BMCK3033 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int outerBank;
    private int innerBank;
    private boolean nromMode;

    public BMCK3033(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void updatePrgBanks() {
        if (nromMode) {
            if ((innerBank & 3) != 0) {
                set4PrgBanks(4, (((outerBank << 3) | innerBank) & 0xFE) << 1);
            } else {
                set2PrgBanks(4, (outerBank << 3) | innerBank);
                set2PrgBanks(6, (outerBank << 3) | innerBank);
            }
        } else {
            super.updatePrgBanks();
        }
    }

    private void writeOuterBank(final int address) {
        outerBank = ((address & 0x40) >> 4) | ((address & 0x18) >> 3);
        innerBank = address & 7;
        nromMode = !getBitBool(address, 5);
        if (nromMode) {
            setPrgBlock(0, -1);
            setChrBlock(0, 0x7F);
        } else {
            if (getBitBool(address, 7)) {
                setPrgBlock(outerBank << 5, 0x1F);
                setChrBlock(outerBank << 8, 0xFF);
            } else {
                setPrgBlock(outerBank << 4, 0x0F);
                setChrBlock(outerBank << 7, 0x7F);
            }
        }
        updateBanks();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            writeOuterBank(address);
        }
        super.writeMemory(address, value);
    }
}
