package nintaco.mappers.unif.unl;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class FARID_SLROM_8IN1 extends MMC1 {

    private static final long serialVersionUID = 0;

    private boolean bankWritesEnabled;
    private int prgOuter;
    private int chrOuter;

    public FARID_SLROM_8IN1(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        bankWritesEnabled = true;
        prgOuter = chrOuter = 0;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            if (prgRamEnabled) {
                if (bankWritesEnabled) {
                    bankWritesEnabled = !getBitBool(value, 3);
                    prgOuter = (value & 0x70) >> 1;
                    chrOuter = prgOuter << 2;
                    updateBanks();
                }
                super.writeMemory(address, value);
            }
        } else {
            super.writeMemory(address, value);
        }
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, chrOuter | (value & 0x1F));
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, prgOuter | (value & 0x07));
    }
}