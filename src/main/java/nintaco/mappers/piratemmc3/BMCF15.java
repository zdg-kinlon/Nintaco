package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class BMCF15 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int reg;

    public BMCF15(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        reg = 0;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void updatePrgBanks() {
        final int bank = reg & 0x0F;
        final int mode = (reg & 0x08) >> 3;
        final int mask = ~mode;
        set2PrgBanks(4, (bank & mask) << 1);
        set2PrgBanks(6, ((bank & mask) | mode) << 1);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            if (prgRamChipEnabled) {
                reg = value;
                updatePrgBanks();
            }
        } else {
            super.writeMemory(address, value);
        }
    }
}
