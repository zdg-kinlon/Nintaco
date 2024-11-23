package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class FARID_UNROM_8IN1 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean clock;
    private boolean latch;
    private int prgInner;
    private int prgOuter;

    public FARID_UNROM_8IN1(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    public void init() {
        prgInner = prgOuter = 0;
        clock = latch = false;
        updatePrgBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        prgInner = value & 7;
        final boolean nextClock = getBitBool(value, 7);
        final boolean nextLatch = getBitBool(value, 3);
        if (nextClock && !clock && !latch) {
            latch = nextLatch;
            prgOuter = ((value >> 4) & 7) << 3;
        }
        clock = nextClock;

        updatePrgBanks();
    }

    private void updatePrgBanks() {
        setPrgBank(2, prgOuter | prgInner);
        setPrgBank(3, prgOuter | 7);
    }
}