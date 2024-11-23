package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class PowerJoy extends MMC3 {

    private static final long serialVersionUID = 0;

    private int reg0, reg1, reg2;

    public PowerJoy(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateBanks() {
        int prgMask;
        int chrMask;
        int prgBank;
        int chrBank;

        if (getBitBool(reg0, 6)) {
            prgMask = 0x0F;
            prgBank = (reg0 & 0x7) << 4;
        } else {
            prgMask = 0x1F;
            prgBank = (reg0 & 0x6) << 4;
        }
        prgBank |= (reg0 & 0x10) << 3;

        if (getBitBool(reg0, 7)) {
            chrMask = 0x7F;
            chrBank = (reg0 & 0x08) << 4;
        } else {
            chrMask = 0xFF;
            chrBank = 0;
        }
        chrBank |= (reg0 & 0x20) << 3;
        chrBank |= (reg0 & 0x10) << 5;

        if (!getBitBool(reg2, 3)) {
            switch (reg2 & 0x3) {
                case 0:
                    setPrgBlockMask(prgMask);
                    setPrgBlockOffset(prgBank);
                    updatePrgBanks();
                    break;
                case 1:
                case 2:
                    setPrgBlockMask(-1);
                    setPrgBlockOffset(0);
                    final int bank = (((prgMode ? 0x3E : R[6]) & prgMask) | prgBank)
                            & 0xFE;
                    setPrgBanks(4, 2, bank);
                    setPrgBanks(6, 2, bank);
                    break;
                case 3:
                    setPrgBlockMask(-1);
                    setPrgBlockOffset(0);
                    setPrgBanks(4, 4, (((prgMode ? 0x3E : R[6]) & prgMask) | prgBank)
                            & 0xFC);
                    break;
            }
        }
        if (getBitBool(reg2, 4)) {
            if (!getBitBool(reg0, 7)) {
                chrBank |= reg1 & 0x80;
            }
            setChrBlockMask(-1);
            setChrBlockOffset(0);
            setChrBanks(0, 8, ((reg1 & 0x0F) | (chrBank >> 3)) << 3);
        } else {
            setChrBlockMask(chrMask);
            setChrBlockOffset(chrBank);
            updateChrBanks();
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xE000) == 0x6000) {
            switch (address & 3) {
                case 0:
                    if (getBitBool(reg2, 7)) {
                        return;
                    }
                    reg0 = value;
                    break;
                case 2:
                    reg1 = value;
                    break;
                case 3:
                    if (getBitBool(reg2, 7)) {
                        return;
                    }
                    reg2 = value;
                    break;
            }
            updateBanks();
        } else if (address >= 0x8000) {
            writeRegister(address, value);
        }
    }
}