package nintaco.mappers.unif.unl;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class SHERO extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean chrRamMode;
    private int register;
    private int resetSwitch;

    public SHERO(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void resetting() {
        resetSwitch ^= 0xFF;
        super.init();
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x2000 && !chrRamMode && chrAddressMask != 0) {
            return chrROM[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & chrRomSizeMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address == 0x4100) {
            register = value;
            chrRamMode = getBitBool(value, 6);
            updateBanks();
        }
        super.writeMemory(address, value);
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x4100) {
            return resetSwitch;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        switch (bank) {
            case 0:
            case 1:
                super.setChrBank(bank, value | ((register & 0x08) << 5));
                break;
            case 2:
            case 3:
                super.setChrBank(bank, value | ((register & 0x04) << 6));
                break;
            case 4:
            case 5:
                super.setChrBank(bank, value | ((register & 0x01) << 8));
                break;
            default:
                super.setChrBank(bank, value | ((register & 0x02) << 7));
                break;
        }
    }
}