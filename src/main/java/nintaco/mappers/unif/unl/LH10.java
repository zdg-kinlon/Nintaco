package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

public class LH10 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[8];

    private int register;

    public LH10(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        register = 0;
        setPrgBank(3, -2);
        setPrgBank(7, -1);
        setChrBank(0);
        updateBanks();
    }

    private void updateBanks() {
        setPrgBank(4, regs[6]);
        setPrgBank(5, regs[7]);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0xC000) {
            return memory[address];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0xE000) != 0xC000) {
            switch (address & 0xE001) {
                case 0x8000:
                    register = value & 7;
                    break;
                case 0x8001:
                    regs[register] = value;
                    updateBanks();
                    break;
            }
        }
    }
}
