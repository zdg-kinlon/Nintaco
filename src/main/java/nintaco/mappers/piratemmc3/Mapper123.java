package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper123 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] security = {0, 3, 1, 5, 6, 7, 2, 4};

    private final int[] regs = new int[2];

    public Mapper123(final CartFile cartFile) {
        super(cartFile);

        // Ultimate Mortal Kombat 3 (14 People) (Unl) [!].nes
        memory[0x5F74] = memory[0x5F75] = 0xFF;
    }

    @Override
    public void updatePrgBanks() {
        if (getBitBool(regs[0], 6)) {
            final int bank = (regs[0] & 0x05) | ((regs[0] & 0x08) >> 2)
                    | ((regs[0] & 0x20) >> 2);
            if (getBitBool(regs[0], 1)) {
                setPrgBanks(4, 4, (bank & 0xFE) << 1);
            } else {
                setPrgBanks(4, 2, bank << 1);
                setPrgBanks(6, 2, bank << 1);
            }
        } else {
            super.updatePrgBanks();
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x8000 || (address >= 0x5001 && address < 0x6000)) {
            writeRegister(address, value);
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if (address < 0x8000 && getBitBool(address, 11)) {
            regs[address & 1] = value;
            updatePrgBanks();
        } else if (address < 0xA000) {
            switch (address & 0x8001) {
                case 0x8000:
                    writeBankSelect((value & 0xC0) | security[value & 7]);
                    break;
                case 0x8001:
                    writeBankData(value);
                    break;
            }
        } else {
            super.writeRegister(address, value);
        }
    }
}
