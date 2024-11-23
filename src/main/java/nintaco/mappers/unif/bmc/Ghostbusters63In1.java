package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class Ghostbusters63In1 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[2];

    private boolean registerEnabled;

    public Ghostbusters63In1(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        registerEnabled = true;
        regs[0] = 0;
        regs[1] = 0;
        setChrBank(0);
        updateBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateBanks() {
        final int offset = ((regs[1] << 5) & 0x20) << (regs[0] >> 7);
        if (offset < (regs[0] >> 7)) {
            registerEnabled = false;
        } else {
            setPrgBank(2, offset | (regs[0] & 0x1E) | ((regs[0] >> 5) & regs[0]));
            setPrgBank(3, offset | (regs[0] & 0x1F) | ((~regs[0] >> 5) & 1));
        }
        setNametableMirroring(getBit(regs[0], 6) ^ 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (registerEnabled) {
            regs[address & 1] = value;
            updateBanks();
        }
    }
}
