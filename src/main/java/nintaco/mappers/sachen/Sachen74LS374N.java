package nintaco.mappers.sachen;

import nintaco.files.*;
import nintaco.mappers.*;

public class Sachen74LS374N extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[8];

    private int register;

    public Sachen74LS374N(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
    }

    private void updateBanks() {
        setChrBank(((regs[2] & 1) << 3) | ((regs[6] & 3) << 1) | (regs[4] & 1));
        setPrgBank(regs[5] & 1);
        setNametableMirroring((regs[7] & 1) ^ 1);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xC101) {
            case 0x4100:
                register = value & 7;
                break;
            case 0x4101:
                regs[register] = value;
                if (register == 0) {
                    setChrBank(3);
                    setPrgBank(0);
                } else {
                    updateBanks();
                }
                break;
        }
    }
}
