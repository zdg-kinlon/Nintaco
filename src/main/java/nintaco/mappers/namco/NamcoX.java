package nintaco.mappers.namco;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.getBitBool;

public class NamcoX extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] NAMETABLE_MIRRORING = {
            ONE_SCREEN_A,
            VERTICAL,
            HORIZONTAL,
            ONE_SCREEN_B,
    };

    protected final Namco163Audio audio = new Namco163Audio();
    protected final boolean[] ntRamEnabled = new boolean[2];
    protected final boolean[] ramWritable = new boolean[4];

    protected int namcoType;
    protected int irqCounter;
    protected boolean irqEnabled;
    protected boolean prgRamEnabled;

    public NamcoX(final CartFile cartFile) {
        super(cartFile, 8, 12);
        setPrgBank(7, -1);
    }

    @Override
    public void init() {
    }

    protected void setNamcoType(final int namcoType) {
        this.namcoType = namcoType;
        if (namcoType == 163) {
            nametableMappingEnabled = false;
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (namcoType == 163) {
            if (address < 0x3000) {
                final int bank = chrBanks[address >> 10];
                if (bank >= 0xE0 && (address >= 0x2000
                        || ntRamEnabled[address >> 12])) {
                    return vram[0x2000 | ((bank & 1) << 10) | (address & 0x03FF)];
                } else {
                    return chrROM[(chrBanks[address >> 10] << 10) | (address & 0x03FF)];
                }
            } else {
                return vram[address];
            }
        } else {
            if (address < 0x2000) {
                return chrROM[(chrBanks[address >> 10] << 10) | (address & 0x03FF)];
            } else {
                return vram[address];
            }
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (namcoType == 163) {
            if (address < 0x3000) {
                final int bank = chrBanks[address >> 10];
                if (bank >= 0xE0 && (address >= 0x2000
                        || ntRamEnabled[address >> 12])) {
                    vram[0x2000 | ((bank & 1) << 10) | (address & 0x03FF)] = value;
                }
            } else {
                vram[address] = value;
            }
        } else {
            vram[address] = value;
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x6000 && address < 0x8000) {
            if (namcoType == 340) {
                setNamcoType(175);
            }
            if (namcoType == 163 || (namcoType == 175 && prgRamEnabled)) {
                return memory[address];
            } else {
                return 0;
            }
        } else if (address >= 0x8000) {
            return prgROM[(prgBanks[address >> 13] | (address & 0x1FFF))
                    & prgRomSizeMask];
        } else {
            final int value = audio.readRegister(address);
            if (value >= 0) {
                setNamcoType(163);
                return value;
            }
            switch (address & 0xF800) {
                case 0x5000:
                    return readIrqCounterLow();
                case 0x5800:
                    return readIrqCounterHigh();
                default:
                    return memory[address];
            }
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x6000 && address < 0x8000) {
            if (namcoType == 340) {
                setNamcoType(175);
            }
            if (namcoType == 163) {
                if (ramWritable[(address >> 11) & 3]) {
                    memory[address] = value;
                }
            } else if (namcoType == 175 && prgRamEnabled) {
                memory[address] = value;
            }
        } else if (address >= 0x4800) {
            audio.writeRegister(address, value);
            switch (address & 0xF800) {
                case 0x4800:
                    setNamcoType(163);
                    break;
                case 0x5000:
                    writeIrqCounterLow(value);
                    break;
                case 0x5800:
                    writeIrqCounterHigh(value);
                    break;
                case 0x8000:
                    writeChrBank(0, value);
                    break;
                case 0x8800:
                    writeChrBank(1, value);
                    break;
                case 0x9000:
                    writeChrBank(2, value);
                    break;
                case 0x9800:
                    writeChrBank(3, value);
                    break;
                case 0xA000:
                    writeChrBank(4, value);
                    break;
                case 0xA800:
                    writeChrBank(5, value);
                    break;
                case 0xB000:
                    writeChrBank(6, value);
                    break;
                case 0xB800:
                    writeChrBank(7, value);
                    break;
                case 0xC000:
                    writePrgRamEnable(value);
                    writeChrBank(8, value);
                    break;
                case 0xC800:
                    writeChrBank(9, value);
                    break;
                case 0xD000:
                    writeChrBank(10, value);
                    break;
                case 0xD800:
                    writeChrBank(11, value);
                    break;
                case 0xE000:
                    writePrgSelect1(value);
                    break;
                case 0xE800:
                    writePrgSelect2(value);
                    break;
                case 0xF000:
                    writePrgSelect3(value);
                    break;
                case 0xF800:
                    writeWriteProtect(value);
                    break;
                default:
                    memory[address] = value;
                    break;
            }
        } else {
            memory[address] = value;
        }
    }

    protected void writeChrBank(final int bank, final int value) {
        chrBanks[bank] = value;
        if (bank == 8) {
            if (namcoType != 163) {
                setNamcoType(175);
            }
        } else if (bank >= 9) {
            setNamcoType(163);
        }
    }

    protected void writePrgSelect1(final int value) {
        writePrgBank(4, value & 0x3F);
        if (getBitBool(value, 7) || (namcoType != 163 && !audio.isSoundEnabled())) {
            setNamcoType(340);
        }
        if (namcoType == 340) {
            setNametableMirroring(NAMETABLE_MIRRORING[value >> 6]);
        }
    }

    protected void writePrgSelect2(final int value) {
        writePrgBank(5, value);
        ntRamEnabled[0] = !getBitBool(value, 6);
        ntRamEnabled[1] = !getBitBool(value, 7);
    }

    protected void writePrgSelect3(final int value) {
        writePrgBank(6, value);
    }

    protected void writePrgBank(final int bank, final int value) {
        setPrgBank(bank, value & 0x3F);
    }

    protected int readIrqCounterLow() {
        return irqCounter & 0xFF;
    }

    protected int readIrqCounterHigh() {
        return irqCounter >> 8;
    }

    protected void writeIrqCounterLow(final int value) {
        cpu.interrupt().setMapperIrq(false);
        irqCounter = (readIrqCounterHigh() << 8) | value;
        setNamcoType(163);
    }

    protected void writeIrqCounterHigh(final int value) {
        cpu.interrupt().setMapperIrq(false);
        irqCounter = readIrqCounterLow() | ((value & 0x7F) << 8);
        irqEnabled = getBitBool(value, 7);
        setNamcoType(163);
    }

    protected void writeWriteProtect(final int value) {
        if ((0xF0 & value) == 0x40) {
            ramWritable[0] = !getBitBool(value, 0);
            ramWritable[1] = !getBitBool(value, 1);
            ramWritable[2] = !getBitBool(value, 2);
            ramWritable[3] = !getBitBool(value, 3);
        } else {
            ramWritable[0] = ramWritable[1] = ramWritable[2] = ramWritable[3]
                    = false;
        }
        setNamcoType(163);
    }

    protected void writePrgRamEnable(final int value) {
        prgRamEnabled = getBitBool(value, 0);
    }

    @Override
    public void update() {
        if (irqEnabled && irqCounter < 0x7FFF && ++irqCounter == 0x7FFF) {
            cpu.interrupt().setMapperIrq(true);
        }
        audio.update();
    }

    @Override
    public int getAudioMixerScale() {
        return audio.getAudioMixerScale();
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }
}