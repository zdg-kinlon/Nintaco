package nintaco.mappers.taito;

import nintaco.files.NesFile;
import nintaco.*;
import nintaco.mappers.*;

public class X1017 extends Mapper {

    private static final long serialVersionUID = 0;

    private final boolean[] ramEnabled = new boolean[3];

    private int chrMode;

    public X1017(NesFile nesFile) {
        super(nesFile, 8, 8);
        setPrgBank(7, -1);
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x2000) {
            return chrROM[chrBanks[(address >> 10) ^ chrMode] | (address & 0x03FF)];
        } else {
            return vram[address];
        }
    }

    @Override
    public int readMemory(int address) {
        if (address >= 0x8000) {
            return prgROM[(prgBanks[(address >> 13)] | (address & 0x1FFF))
                    & prgRomSizeMask];
        } else if (address >= 0x6000 && address < 0x7400) {
            if (address < 0x6800) {
                return ramEnabled[0] ? memory[address] : 0;
            } else if (address < 0x7000) {
                return ramEnabled[1] ? memory[address] : 0;
            } else {
                return ramEnabled[2] ? memory[address] : 0;
            }
        } else {
            return memory[address];
        }
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x6000 && address < 0x7400) {
            if (address < 0x6800) {
                if (ramEnabled[0]) {
                    memory[address] = value;
                }
            } else if (address < 0x7000) {
                if (ramEnabled[1]) {
                    memory[address] = value;
                }
            } else {
                if (ramEnabled[2]) {
                    memory[address] = value;
                }
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
                    writeChrMode(value);
                    break;
                case 0x7EF7:
                    ramEnabled[0] = value == 0xCA;
                    break;
                case 0x7EF8:
                    ramEnabled[1] = value == 0x69;
                    break;
                case 0x7EF9:
                    ramEnabled[2] = value == 0x84;
                    break;
                case 0x7EFA:
                    writePrgBank(4, value);
                    break;
                case 0x7EFB:
                    writePrgBank(5, value);
                    break;
                case 0x7EFC:
                    writePrgBank(6, value);
                    break;
                default:
                    memory[address] = value;
                    break;
            }
        }
    }

    private void writePrgBank(int bank, int value) {
        prgBanks[bank] = (value & 0xFC) << 11;
    }

    private void writeChrMode(int value) {
        setNametableMirroring((value & 1) ^ 1);
        chrMode = (value & 2) << 1;
    }

    private void writeChrBank(int bank, int value) {
        if (bank < 4) {
            setChrBank(bank, value);
            setChrBank(bank + 1, value + 1);
        } else {
            setChrBank(bank, value);
        }
    }
}
