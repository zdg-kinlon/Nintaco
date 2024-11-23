package nintaco.mappers.sachen;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.mappers.NametableMirroring.*;

public class Sachen74LS374Nb extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] NAMETABLE_MIRRORINGS = {
            HORIZONTAL,
            VERTICAL,
            L_SHAPED,
            ONE_SCREEN_A,
    };

    private final int[] regs = new int[8];

    private int resetBit;
    private int register;

    public Sachen74LS374Nb(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
    }

    @Override
    public void resetting() {
        resetBit ^= 1;
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xC101) == 0x4000) {
            return (~register) ^ resetBit;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xC101) {
            case 0x4100:
                register = value & 0x07;
                break;
            case 0x4101:
                regs[register] = value;
                updateBanks();
                break;
        }
    }

    private void updateBanks() {
        setChrBank(((regs[2] & 1) << 3) | ((regs[4] & 1) << 2) | (regs[6] & 3));
        if (register == 2) {
            setPrgBank(regs[2] & 1);
        } else {
            setPrgBank(regs[5] & 7);
        }
        setNametableMirroring(NAMETABLE_MIRRORINGS[(regs[7] >> 1) & 3]);
    }
}
