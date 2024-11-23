package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper219 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[3];

    public Mapper219(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        setPrgBanks(4, 4, -4);
        setChrBanks(0, 8, 0);
    }

    @Override
    protected void updatePrgBanks() {
    }

    @Override
    protected void updateChrBanks() {
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if (address < 0xA000) {
            switch (address & 0xE003) {
                case 0x8000:
                    regs[0] = 0;
                    regs[1] = value;
                    break;

                case 0x8001:
                    if (regs[0] >= 0x23 && regs[0] <= 0x26) {
                        setPrgBank(0x2A - regs[0], ((value & 0x20) >> 5)
                                | ((value & 0x10) >> 3) | ((value & 0x08) >> 1)
                                | ((value & 0x04) << 1));
                    }

                    switch (regs[1]) {
                        case 0x08:
                        case 0x0A:
                        case 0x0E:
                        case 0x12:
                        case 0x16:
                        case 0x1A:
                        case 0x1E:
                            regs[2] = value << 4;
                            break;
                        case 0x09:
                            setChrBank(0, regs[2] | (value >> 1 & 0x0E));
                            break;
                        case 0x0B:
                            setChrBank(1, regs[2] | (value >> 1 | 0x01));
                            break;
                        case 0x0C:
                        case 0x0D:
                            setChrBank(2, regs[2] | (value >> 1 & 0x0E));
                            break;
                        case 0x0F:
                            setChrBank(3, regs[2] | (value >> 1 | 0x01));
                            break;
                        case 0x10:
                        case 0x11:
                            setChrBank(4, regs[2] | (value >> 1 & 0x0F));
                            break;
                        case 0x14:
                        case 0x15:
                            setChrBank(5, regs[2] | (value >> 1 & 0x0F));
                            break;
                        case 0x18:
                        case 0x19:
                            setChrBank(6, regs[2] | (value >> 1 & 0x0F));
                            break;
                        case 0x1C:
                        case 0x1D:
                            setChrBank(7, regs[2] | (value >> 1 & 0x0F));
                            break;
                    }
                    break;

                case 0x8002:
                    regs[0] = value;
                    regs[1] = 0;
                    break;
            }
        } else {
            super.writeRegister(address, value);
        }
    }
}