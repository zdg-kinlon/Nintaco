package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper269 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] UNSCRAMBLE = new int[256];

    static {
        for (int i = UNSCRAMBLE.length - 1; i >= 0; --i) {
            UNSCRAMBLE[i] = ((i & 1) << 6) | ((i & 2) << 3) | (i & 4) | ((i & 8) >> 3)
                    | ((i & 16) >> 3) | ((i & 32) >> 2) | ((i & 64) >> 1) | (i & 128);
        }
    }

    private final int[] reg = new int[4];

    private int chrMask;
    private int regIndex;

    public Mapper269(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        reg[0] = reg[1] = reg[3] = regIndex = 0;
        reg[2] = 0x0F;
        chrMask = 0xFF;
        updateState();
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private int getChrBank(final int bank) {
        if (chrMode) {
            switch (bank) {
                case 0:
                    return R[2];
                case 1:
                    return R[3];
                case 2:
                    return R[4];
                case 3:
                    return R[5];
                case 4:
                    return R[0] & 0xFE;
                case 5:
                    return R[0] | 0x01;
                case 6:
                    return R[1] & 0xFE;
                default:
                    return R[1] | 0x01;
            }
        } else {
            switch (bank) {
                case 0:
                    return R[0] & 0xFE;
                case 1:
                    return R[0] | 0x01;
                case 2:
                    return R[1] & 0xFE;
                case 3:
                    return R[1] | 0x01;
                case 4:
                    return R[2];
                case 5:
                    return R[3];
                case 6:
                    return R[4];
                default:
                    return R[5];
            }
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return UNSCRAMBLE[prgROM[((((getChrBank(address >> 10) & chrMask)
                    + ((reg[0] & ~chrMask) | ((reg[2] & 0xF0) << 4)
                    | ((reg[3] & 0xC0) << 6))) << 10) | (address & 0x03FF))
                    & prgRomSizeMask]];
        } else {
            return super.readVRAM(address);
        }
    }

    private void updateState() {
        final int prgMask = ~reg[3] & 0x3F;
        setPrgBlock((reg[1] & ~prgMask) | ((reg[3] & 0xC0) << 2), prgMask);
    }

    private void writeOuterBankRegisters(final int value) {
        if (regIndex == 2) {
            chrMask = ((value & 8) != 0) ? (0xFF >> (~value & 7)) : 0xFF;
        }
        reg[regIndex++] = value;
        regIndex &= 3;
        updateState();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address == 0x5000) {
            writeOuterBankRegisters(value);
        }
        super.writeMemory(address, value);
    }
}
