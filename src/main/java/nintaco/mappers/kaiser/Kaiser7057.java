package nintaco.mappers.kaiser;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Kaiser7057 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[8];

    public Kaiser7057(final CartFile cartFile) {
        super(cartFile, 32, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setChrBank(0);
        for (int i = 20; i <= 31; i++) {
            setPrgBank(i, 0x20 + i);
        }
        updateBanks();
    }

    private void updateBanks() {
        for (int i = 3; i >= 0; i--) {
            setPrgBank(i + 12, regs[i + 4]);
            setPrgBank(i + 16, regs[i]);
        }
    }

    private void setReg(final int index, final int value, final boolean low) {

        if (low) {
            regs[index] = (regs[index] & 0xF0) | (value & 0x0F);
        } else {
            regs[index] = (regs[index] & 0x0F) | ((value << 4) & 0xF0);
        }

        updateBanks();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final boolean low = (address & 1) == 0;
        switch (address & 0xF002) {
            case 0x8000:
            case 0x8002:
            case 0x9000:
            case 0x9002:
                setNametableMirroring((value & 1) ^ 1);
                break;

            case 0xB000:
                setReg(0, value, low);
                break;
            case 0xB002:
                setReg(1, value, low);
                break;
            case 0xC000:
                setReg(2, value, low);
                break;
            case 0xC002:
                setReg(3, value, low);
                break;
            case 0xD000:
                setReg(4, value, low);
                break;
            case 0xD002:
                setReg(5, value, low);
                break;
            case 0xE000:
                setReg(6, value, low);
                break;
            case 0xE002:
                setReg(7, value, low);
                break;
        }
    }
}
