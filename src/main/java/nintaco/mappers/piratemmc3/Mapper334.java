package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper334 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int prgRomBank;
    private int dip;

    public Mapper334(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void resetting() {
        dip ^= 1;
    }

    @Override
    protected void updatePrgBanks() {
        set4PrgBanks(4, prgRomBank);
    }

    private void writePrgRomBank(final int value) {
        prgRomBank = (value & 0x06) << 1;
        updateBanks();
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x6002) {
            return dip;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE001) == 0x6000) {
            writePrgRomBank(value);
        }
        super.writeMemory(address, value);
    }
}