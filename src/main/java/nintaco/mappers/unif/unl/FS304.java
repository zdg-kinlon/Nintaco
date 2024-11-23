package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

public class FS304 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = {3, 0, 0, 7};

    public FS304(final CartFile cartFile) {
        super(cartFile, 2, 0);
    }

    @Override
    public void init() {
        updateBanks();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF000) == 0x5000) {
            regs[(address >> 8) & 3] = value;
            updateBanks();
        } else {
            super.writeMemory(address, value);
        }
    }

    private void updateBanks() {
        final int prgBank;
        switch (regs[3] & 7) {
            case 0:
            case 2:
                prgBank = (regs[0] & 0x0C) | (regs[1] & 2) | ((regs[2] & 0x0F) << 4);
                break;
            case 1:
            case 3:
                prgBank = (regs[0] & 0x0C) | ((regs[2] & 0x0F) << 4);
                break;
            case 4:
            case 6:
                prgBank = (regs[0] & 0x0E) | ((regs[1] >> 1) & 1)
                        | ((regs[2] & 0x0F) << 4);
                break;
            default:
                prgBank = (regs[0] & 0x0F) | ((regs[2] & 0x0F) << 4);
                break;
        }
        setPrgBank(1, prgBank);
    }
}