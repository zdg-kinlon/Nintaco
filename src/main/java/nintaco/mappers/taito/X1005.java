package nintaco.mappers.taito;

import nintaco.files.*;
import nintaco.mappers.*;

public class X1005 extends Mapper {

    private static final long serialVersionUID = 0;

    protected boolean ramEnabled;

    public X1005(final CartFile cartFile) {
        super(cartFile, 8, 8);
        setPrgBank(7, -1);
    }

    @Override
    public int readMemory(int address) {
        if (address >= 0x7F00 && address < 0x8000) {
            return ramEnabled ? memory[address & 0x7F7F] : 0;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x7F00 && address < 0x8000) {
            if (ramEnabled) {
                memory[address & 0x7F7F] = value;
            }
        } else {
            switch (address) {
                case 0x7EF0:
                    writeChrBank(0, value);
                    break;
                case 0x7EF1:
                    writeChrBank(2, value);
                    break;
                case 0x7EF2:
                    writeChrBank(4, value);
                    break;
                case 0x7EF3:
                    writeChrBank(5, value);
                    break;
                case 0x7EF4:
                    writeChrBank(6, value);
                    break;
                case 0x7EF5:
                    writeChrBank(7, value);
                    break;
                case 0x7EF6:
                case 0x7EF7:
                    setNametableMirroring((value & 1) ^ 1);
                    break;
                case 0x7EF8:
                case 0x7EF9:
                    ramEnabled = 0xA3 == value;
                    break;
                case 0x7EFA:
                case 0x7EFB:
                    setPrgBank(4, value);
                    break;
                case 0x7EFC:
                case 0x7EFD:
                    setPrgBank(5, value);
                    break;
                case 0x7EFE:
                case 0x7EFF:
                    setPrgBank(6, value);
                    break;
                default:
                    memory[address] = value;
                    break;
            }
        }
    }

    protected void writeChrBank(int bank, int value) {
        value <<= 10;
        if (bank < 4) {
            chrBanks[bank] = value;
            chrBanks[bank + 1] = value + 0x0400;
        } else {
            chrBanks[bank] = value;
        }
    }
}
