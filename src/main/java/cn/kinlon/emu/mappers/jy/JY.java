package cn.kinlon.emu.mappers.jy;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import java.util.Arrays;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class JY extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] multipliers = new int[2];
    private final int[] commands = new int[4];
    private final int[] prgRegs = new int[4];

    private final int[] chrLows = new int[8];
    private final int[] chrHighs = new int[8];
    private final int[] chrLatches = new int[2];

    private final int[] ntRegs = new int[4];
    private final int[] ntAddress = new int[4];
    private final boolean[] ntRAM = new boolean[4];

    private final boolean mapper209;
    private final boolean mapper211;

    protected int dipswitches;
    protected int irqIncrement;
    protected int irqSource;
    protected int irqXOR;
    protected int irqCounter;
    protected int irqPrescaler;
    protected boolean irqPrescaler3BitMode;
    protected boolean irqEnabled;
    protected boolean lastA12;
    private int reg5803;

    public JY(final CartFile cartFile, final int mapperNumber) {
        super(cartFile, 8, 8);

        switch (mapperNumber) {
            case 209:
                mapper209 = true;
                mapper211 = false;
                break;
            case 211:
                mapper209 = false;
                mapper211 = true;
                dipswitches = 0xC0;
                break;
            default:
                mapper209 = false;
                mapper211 = false;
                break;
        }

        multipliers[0] = multipliers[1] = reg5803 = 0xFF;
        Arrays.fill(prgRegs, 0xFF);
        Arrays.fill(chrLows, 0xFF);
        Arrays.fill(chrHighs, 0xFF);
    }

    @Override
    public void init() {
        chrLatches[0] = 0;
        chrLatches[1] = 4;
        updatePrgBanks();
        updateChrBanks();
    }

    @Override
    public void resetting() {
        dipswitches = (dipswitches + 0x40) & 0xC0;
        Arrays.fill(commands, 0x00);
        Arrays.fill(prgRegs, 0xFF);
        init();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xF000) {
            case 0x5000:
                writeMultiplier(address, value);
                break;
            case 0x8000:
                if (address <= 0x8FF0) {
                    writePrg(address, value);
                }
                break;
            case 0x9000:
                writeChrLow(address, value);
                break;
            case 0xA000:
                writeChrHigh(address, value);
                break;
            case 0xB000:
                writeNametables(address, value);
                break;
            case 0xC000:
                writeIrq(address, value);
                break;
            case 0xD000:
                if (address < 0xD600) {
                    writeMode(address, value);
                }
                break;
        }
        if (irqEnabled && irqSource == 3) {
            incrementIrqPrescaler();
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x6000) {
            return prgROM[(prgBanks[address >> 13] | (address & 0x1FFF))
                    & prgRomSizeMask];
        } else if (address >= 0x5000) {
            return readMultiplier(address);
        } else {
            return memory[address];
        }
    }

    @Override
    public int readVRAM(final int address) {

        if (irqEnabled && irqSource == 2) {
            incrementIrqPrescaler();
        }

        if (mapper209) {
            final int high = address >> 8;
            if (high < 0x20 && ((high & 0x0F) == 0x0F)) {
                final int low = address & 0xF0;
                if (low == 0xD0) {
                    chrLatches[(high & 0x10) >> 4] = ((high & 0x10) >> 2);
                    updateChrBanks();
                } else if (low == 0xE0) {
                    chrLatches[(high & 0x10) >> 4] = ((high & 0x10) >> 2) | 2;
                    updateChrBanks();
                }
            }
        }

        if (!nametableMappingEnabled && address >= 0x2000 && address <= 0x3EFF) {
            final int index = (address >> 10) & 3;
            final int addr = ntAddress[index] | (address & 0x03FF);
            if (ntRAM[index]) {
                return vram[addr];
            } else {
                return chrROM[addr & chrRomSizeMask];
            }
        } else {
            return super.readVRAM(address);
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (!nametableMappingEnabled && address >= 0x2000 && address <= 0x3EFF) {
            final int index = (address >> 10) & 3;
            if (ntRAM[index]) {
                vram[ntAddress[index] | (address & 0x03FF)] = value;
            }
        } else {
            super.writeVRAM(address, value);
        }
    }

    public void updateMirroring() {
        if ((getBitBool(commands[0], 5) && mapper209) || mapper211) {
            nametableMappingEnabled = false;
            if (getBitBool(commands[0], 6)) {
                // NT ROM only
                for (int x = 3; x >= 0; x--) {
                    ntRAM[x] = false;
                    ntAddress[x] = ntRegs[x] << 10;
                }
            } else {
                // NT RAM or ROM
                for (int x = 3; x >= 0; x--) {
                    if ((commands[1] & 0x80) == (ntRegs[x] & 0x80)) {
                        // NT USES SCREEN A OR B
                        ntRAM[x] = true;
                        ntAddress[x] = 0x2000 | ((ntRegs[x] & 1) << 10);
                    } else {
                        // NT USES CHR ROM
                        ntRAM[x] = false;
                        ntAddress[x] = ntRegs[x] << 10;
                    }
                }
            }
        } else {
            nametableMappingEnabled = true;
            setNametableMirroring(commands[1] & 3);
        }
    }

    protected void updatePrgBanks() {
        final int bankMode = ((commands[3] & 6) << 5);
        switch (commands[0] & 7) {
            case 0:
                if (getBitBool(commands[0], 7)) {
                    setPrgBank(3, (((prgRegs[3] << 2) + 3) & 0x3F) | bankMode);
                }
                setPrgBanks(4, 4, (0x0F | ((commands[3] & 6) << 3)) << 2);
                break;
            case 1:
                if (getBitBool(commands[0], 7)) {
                    setPrgBank(3, (((prgRegs[3] << 1) + 1) & 0x3F) | bankMode);
                }
                setPrgBanks(4, 2, ((prgRegs[1] & 0x1F) | ((commands[3] & 6) << 4)) << 1);
                setPrgBanks(6, 2, (0x1F | ((commands[3] & 6) << 4)) << 1);
                break;
            case 3:
            case 2:
                if (getBitBool(commands[0], 7)) {
                    setPrgBank(3, (prgRegs[3] & 0x3F) | bankMode);
                }
                setPrgBank(4, (prgRegs[0] & 0x3F) | bankMode);
                setPrgBank(5, (prgRegs[1] & 0x3F) | bankMode);
                setPrgBank(6, (prgRegs[2] & 0x3F) | bankMode);
                setPrgBank(7, 0x3F | bankMode);
                break;
            case 4:
                if (getBitBool(commands[0], 7)) {
                    setPrgBank(3, (((prgRegs[3] << 2) + 3) & 0x3F) | bankMode);
                }
                setPrgBanks(4, 4, ((prgRegs[3] & 0x0F) | ((commands[3] & 6) << 3)) << 2);
                break;
            case 5:
                if (getBitBool(commands[0], 7)) {
                    setPrgBank(3, (((prgRegs[3] << 1) + 1) & 0x3F) | bankMode);
                }
                setPrgBanks(4, 2, ((prgRegs[1] & 0x1F) | ((commands[3] & 6) << 4)) << 1);
                setPrgBanks(6, 2, ((prgRegs[3] & 0x1F) | ((commands[3] & 6) << 4)) << 1);
                break;
            case 7:
            case 6:
                if (getBitBool(commands[0], 7)) {
                    setPrgBank(3, (prgRegs[3] & 0x3F) | bankMode);
                }
                setPrgBank(4, (prgRegs[0] & 0x3F) | bankMode);
                setPrgBank(5, (prgRegs[1] & 0x3F) | bankMode);
                setPrgBank(6, (prgRegs[2] & 0x3F) | bankMode);
                setPrgBank(7, (prgRegs[3] & 0x3F) | bankMode);
                break;
        }
    }

    protected void updateChrBanks() {
        int bank = 0;
        int mask = 0xFFFF;
        if (!getBitBool(commands[3], 5)) {
            bank = (commands[3] & 1) | ((commands[3] & 0x18) >> 2);
            switch (commands[0] & 0x18) {
                case 0x00:
                    bank <<= 5;
                    mask = 0x1F;
                    break;
                case 0x08:
                    bank <<= 6;
                    mask = 0x3F;
                    break;
                case 0x10:
                    bank <<= 7;
                    mask = 0x7F;
                    break;
                case 0x18:
                    bank <<= 8;
                    mask = 0xFF;
                    break;
            }
        }
        switch (commands[0] & 0x18) {
            case 0x00:
                setChrBanks(0, 8,
                        (((chrLows[0] | (chrHighs[0] << 8)) & mask) | bank) << 3);
                break;
            case 0x08:
                setChrBanks(0, 4, (((chrLows[chrLatches[0]] | (chrHighs[chrLatches[0]]
                        << 8)) & mask) | bank) << 2);
                setChrBanks(4, 4, (((chrLows[chrLatches[1]] | (chrHighs[chrLatches[1]]
                        << 8)) & mask) | bank) << 2);
                break;
            case 0x10:
                for (int x = 6; x >= 0; x -= 2) {
                    setChrBanks(x, 2,
                            (((chrLows[x] | (chrHighs[x] << 8)) & mask) | bank) << 1);
                }
                break;
            case 0x18:
                for (int x = 7; x >= 0; x--) {
                    setChrBank(x, ((chrLows[x] | (chrHighs[x] << 8)) & mask) | bank);
                }
                break;
        }
    }

    private void writeMultiplier(final int address, final int value) {
        switch (address & 0x5C03) {
            case 0x5800:
                multipliers[0] = value;
                break;
            case 0x5801:
                multipliers[1] = value;
                break;
            case 0x5803:
                reg5803 = value;
                break;
        }
    }

    private int readMultiplier(final int address) {
        switch (address & 0x5C03) {
            case 0x5800:
                return (multipliers[0] * multipliers[1]) & 0xFF;
            case 0x5801:
                return ((multipliers[0] * multipliers[1]) >> 8) & 0xFF;
            case 0x5803:
                return reg5803;
            default:
                return dipswitches;
        }
    }

    private void writePrg(final int address, final int value) {
        prgRegs[address & 3] = value;
        updatePrgBanks();
    }

    private void writeChrLow(final int address, final int value) {
        chrLows[address & 7] = value;
        updateChrBanks();
    }

    private void writeChrHigh(final int address, final int value) {
        chrHighs[address & 7] = value;
        updateChrBanks();
    }

    private void writeNametables(final int address, final int value) {
        if (getBitBool(address, 2)) {
            ntRegs[address & 3] &= 0x00FF;
            ntRegs[address & 3] |= value << 8;
        } else {
            ntRegs[address & 3] &= 0xFF00;
            ntRegs[address & 3] |= value;
        }
        updateMirroring();
    }

    private void writeIrq(final int address, final int value) {
        switch (address & 7) {
            case 0:
                writeIrqEnable(value);
                break;
            case 1:
                writeIrqMode(value);
                break;
            case 2:
                writeIrqAcknowledge();
                break;
            case 3:
                writeIrqEnable();
                break;
            case 4:
                writeIrqPrescaler(value);
                break;
            case 5:
                writeIrqCounter(value);
                break;
            case 6:
                writeIrqXOR(value);
                break;
        }
    }

    private void writeMode(final int address, final int value) {
        commands[address & 3] = value;
        updatePrgBanks();
        updateChrBanks();
        updateMirroring();
    }

    protected void writeIrqMode(final int value) {
        switch (value >> 6) {
            case 1:
                irqIncrement = 1;
                break;
            case 2:
                irqIncrement = -1;
                break;
            default:
                irqIncrement = 0;
                break;
        }
        irqPrescaler3BitMode = getBitBool(value, 2);
        irqSource = value & 3;
    }

    protected void writeIrqAcknowledge() {
        cpu.setMapperIrq(false);
        irqEnabled = false;
    }

    protected void writeIrqEnable(final int value) {
        if (getBitBool(value, 0)) {
            writeIrqEnable();
        } else {
            writeIrqAcknowledge();
        }
    }

    protected void writeIrqEnable() {
        irqEnabled = true;
    }

    protected void writeIrqPrescaler(final int value) {
        irqPrescaler = (value ^ irqXOR) & 0xFF;
    }

    protected void writeIrqCounter(final int value) {
        irqCounter = (value ^ irqXOR) & 0xFF;
    }

    protected void writeIrqXOR(final int value) {
        irqXOR = value;
    }

    protected void incrementIrqPrescaler() {
        if (irqIncrement == 1) {
            if (irqPrescaler3BitMode) {
                if ((irqPrescaler & 0x07) == 0x07) {
                    irqPrescaler &= 0xF8;
                    incrementIrqCounter();
                } else {
                    irqPrescaler = (irqPrescaler & 0xF8) | ((irqPrescaler & 0x07) + 1);
                }
            } else {
                if (irqPrescaler == 0xFF) {
                    irqPrescaler = 0;
                    incrementIrqCounter();
                } else {
                    irqPrescaler++;
                }
            }
        } else if (irqIncrement == -1) {
            if (irqPrescaler3BitMode) {
                if ((irqPrescaler & 0x07) == 0x00) {
                    irqPrescaler |= 0x07;
                    incrementIrqCounter();
                } else {
                    irqPrescaler = (irqPrescaler & 0xF8) | ((irqPrescaler & 0x07) - 1);
                }
            } else {
                if (irqPrescaler == 0x00) {
                    irqPrescaler = 0xFF;
                    incrementIrqCounter();
                } else {
                    irqPrescaler--;
                }
            }
        }
    }

    protected void incrementIrqCounter() {
        if (irqIncrement == 1) {
            if (irqCounter == 0xFF) {
                irqCounter = 0;
                if (irqEnabled) {
                    cpu.setMapperIrq(true);
                }
            } else {
                irqCounter++;
            }
        } else if (irqIncrement == -1) {
            if (irqCounter == 0x00) {
                irqCounter = 0xFF;
                if (irqEnabled) {
                    cpu.setMapperIrq(true);
                }
            } else {
                irqCounter--;
            }
        }
    }

    @Override
    public void update() {
        if (irqEnabled && irqSource == 0) {
            incrementIrqPrescaler();
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        final boolean a12 = (address & 0x1000) != 0;
        if (irqEnabled && irqSource == 1 && !lastA12 && a12) {
            incrementIrqPrescaler();
        }
        lastA12 = a12;
    }
}
