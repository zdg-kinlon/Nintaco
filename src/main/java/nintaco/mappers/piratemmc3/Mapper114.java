package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper114 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] security = {0, 3, 1, 5, 6, 7, 2, 4};

    private final int[] regs = new int[2];

    private boolean bankSelected;

    public Mapper114(final CartFile cartFile) {
        super(cartFile);
        mmc3a = true;
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        if (getBitBool(regs[0], 7)) {
            final int b = (regs[0] & 0x0F) << 1;
            super.setPrgBank(4, b);
            super.setPrgBank(5, b | 1);
            super.setPrgBank(6, b);
            super.setPrgBank(7, b | 1);
        } else {
            super.setPrgBank(bank, value & 0x3F);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;

        if (address >= 0x8000) {
            switch (address & 0xE001) {
                case 0x8001:
                    writeMirroring(value);
                    break;
                case 0xA000:
                    writeBankSelect((value & 0xC0) | security[value & 7]);
                    bankSelected = true;
                    break;
                case 0xC000:
                    if (bankSelected) {
                        writeBankData(value);
                        bankSelected = false;
                    }
                    break;
                case 0xA001:
                    irqReloadValue = value;
                    break;
                case 0xC001:
                    irqReloadRequest = true;
                    break;
                case 0xE000:
                    cpu.setMapperIrq(false);
                    irqEnabled = false;
                    break;
                case 0xE001:
                    irqEnabled = true;
                    break;
            }
        } else if (address >= 0x5000) {
            regs[0] = value;
            updatePrgBanks();
        }
    }
}