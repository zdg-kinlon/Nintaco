package cn.kinlon.emu.mappers.taito;

import cn.kinlon.emu.files.CartFile;


import static cn.kinlon.emu.utils.BitUtil.*;

public class X1005b extends X1005 {

    private static final long serialVersionUID = 0;

    public X1005b(final CartFile cartFile) {
        super(cartFile);
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

    @Override
    protected void writeChrBank(int bank, int value) {
        if (bank < 4) {
            chrBanks[bank] = (value & 0x7F) << 10;
            chrBanks[bank + 1] = chrBanks[bank] + 0x0400;
            nametableMappings[bank] = nametableMappings[bank + 1]
                    = getBitBool(value, 7) ? 0x2000 : 0x2400;
        } else {
            chrBanks[bank] = value << 10;
        }
    }
}
