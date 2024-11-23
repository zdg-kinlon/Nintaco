package nintaco.mappers.unif.bmc;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.*;

public class BMC830134C extends MMC3 {

    private static final long serialVersionUID = 0;

    private int outerBank;
    private boolean gnromMode;

    public BMC830134C(final CartFile cartFile) {
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
        if (gnromMode) {
            setPrgBank(4, (outerBank & 0xFD) | (R[6] & 0x0F));
            setPrgBank(5, (outerBank & 0xFD) | (R[7] & 0x0F));
            setPrgBank(6, outerBank | (R[6] & 0x0F) | 0x02);
            setPrgBank(7, outerBank | (R[7] & 0x0F) | 0x02);
        } else {
            super.updatePrgBanks();
        }
    }

    private void writeOuterBank(final int value) {
        outerBank = value & 0x06;
        gnromMode = (outerBank == 0x06);
        outerBank <<= 3;
        if (gnromMode) {
            setPrgBlock(0, -1);
        } else {
            setPrgBlock(outerBank, 0x0F);
        }
        setChrBlock(((value & 0x01) << 8) | ((value & 0x02) << 6)
                | ((value & 0x08) << 3), 0xFF);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            writeOuterBank(value);
        }
        super.writeMemory(address, value);
    }
}
