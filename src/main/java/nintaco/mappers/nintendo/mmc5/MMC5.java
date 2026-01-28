package nintaco.mappers.nintendo.mmc5;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;
import nintaco.tv.TVSystem;

import static nintaco.PPU.REG_PPU_MASK;
import static nintaco.util.BitUtil.getBitBool;

public class MMC5 extends Mapper {

    private static final long serialVersionUID = 0;

    private final boolean[] prgRamBanks = new boolean[8];
    private final int[] prgRegs = new int[4];
    private final int[] chrRegs = new int[12];
    private final int[][] cBanks = new int[2][8];
    private final int[][] cRegs = new int[2][8];

    private final MMC5Audio audio = new MMC5Audio(this);

    private int prgMode;
    private int chrMode;
    private int nametableOffset;
    private int patternTableBlock;
    private int backgroundTileX = -1;
    private int backgroundTileSkip = 2;
    private int extendedRamMode;
    private int fillModeTile;
    private int fillModeAttribute;
    private int upperChrBits;
    private int verticalSplitTile;
    private int verticalSplitScrollValue;
    private int verticalSplitBank;
    private int verticalFetch;
    private int verticalOffset;
    private int irqCounter;
    private int irqScanline;
    private boolean irqPending;
    private boolean irqEnabled;
    private boolean prgRamWriteProtect1;
    private boolean prgRamWriteProtect2;
    private boolean prgRamWriteEnabled;
    private boolean useLowerChrRegs;
    private boolean inFrame;
    private boolean verticalSplitEnabled;
    private boolean verticalSplitRightSide;
    private boolean spriteTile;

    public MMC5(final CartFile cartFile) {
        super(cartFile, 8, 8);
        xram = new int[0x10000];
        prgRamBanks[3] = true;
        nametableMappingEnabled = false;
    }

    @Override
    public void init() {
        chrBanks = cBanks[0];
        writePrgMode(0x03);
        for (int i = 3; i >= 0; i--) {
            writePrgReg(i, 0xFF);
        }
        for (int i = cBanks.length - 1; i >= 0; i--) {
            for (int j = cBanks[i].length - 1; j >= 0; j--) {
                setChrBank(i, j, j);
            }
        }
        writeChrMode(0);
        audio.init();
    }

    @Override
    public void setTVSystem(final TVSystem tvSystem) {
        super.setTVSystem(tvSystem);
        audio.setTVSystem(tvSystem);
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            if (inFrame && verticalFetch > 0 && !spriteTile) {
                verticalFetch--;
                return chrROM[(verticalSplitBank | (address & 0x0FF8)
                        | (verticalOffset & 7)) & chrRomSizeMask];
            } else if (inFrame && extendedRamMode == 1 && !spriteTile) {
                return chrROM[((upperChrBits << 18) | patternTableBlock
                        | (address & 0x0FFF)) & chrRomSizeMask];
            } else {
                return super.readVRAM(address);
            }
        } else if (address < 0x3000) {
            final int offset = address & 0x03FF;
            final boolean isAttribute = offset >= 960;
            if (inFrame && !spriteTile) {
                if (extendedRamMode == 1) {
                    if (isAttribute) {
                        final int exramValue = memory[0x5C00 | nametableOffset];
                        int attribute = exramValue >> 6;
                        attribute |= attribute << 2;
                        attribute |= attribute << 4;
                        patternTableBlock = (exramValue & 0x3F) << 12;
                        return attribute;
                    } else {
                        nametableOffset = offset;
                    }
                }
                if (!isAttribute) {
                    if (backgroundTileX == 2 && backgroundTileSkip > 0) {
                        backgroundTileSkip--;
                    } else {
                        backgroundTileX++;
                    }
                }
                if (verticalSplitEnabled && extendedRamMode < 2
                        && ((verticalSplitRightSide && backgroundTileX >= verticalSplitTile)
                        || (!verticalSplitRightSide
                        && backgroundTileX < verticalSplitTile))) {
                    if (isAttribute) {
                        verticalFetch = 2;
                        return memory[0x5FC0 + (backgroundTileX >> 2)
                                + ((verticalOffset >> 5) << 3)];
                    } else {
                        return memory[0x5C00 + backgroundTileX
                                + ((verticalOffset >> 3) << 5)];
                    }
                }
            }
            switch (nametableMappings[(address >> 10) & 3]) {
                case 0:
                    return vram[0x2000 | offset];
                case 1:
                    return vram[0x2400 | offset];
                case 2:
                    if (extendedRamMode < 2) {
                        return memory[0x5C00 | offset];
                    } else {
                        return 0;
                    }
                default:
                    return isAttribute ? fillModeAttribute : fillModeTile;
            }
        }

        return vram[address];
    }

    @Override
    public int peekVRAM(final int address) {
        if (address < 0x2000) {
            if (inFrame && verticalFetch > 0 && !spriteTile) {
                return chrROM[(verticalSplitBank | (address & 0x0FF8)
                        | (verticalOffset & 7)) & chrRomSizeMask];
            } else if (inFrame && extendedRamMode == 1 && !spriteTile) {
                return chrROM[((upperChrBits << 18) | patternTableBlock
                        | (address & 0x0FFF)) & chrRomSizeMask];
            } else {
                return super.readVRAM(address);
            }
        } else if (address < 0x3000) {
            final int offset = address & 0x03FF;
            final boolean isAttribute = offset >= 960;
            if (inFrame && !spriteTile) {
                if (extendedRamMode == 1) {
                    if (isAttribute) {
                        final int exramValue = memory[0x5C00 | nametableOffset];
                        int attribute = exramValue >> 6;
                        attribute |= attribute << 2;
                        attribute |= attribute << 4;
                        patternTableBlock = (exramValue & 0x3F) << 12;
                        return attribute;
                    } else {
                        nametableOffset = offset;
                    }
                }
                if (verticalSplitEnabled && extendedRamMode < 2
                        && ((verticalSplitRightSide && backgroundTileX >= verticalSplitTile)
                        || (!verticalSplitRightSide
                        && backgroundTileX < verticalSplitTile))) {
                    if (isAttribute) {
                        return memory[0x5FC0 + (backgroundTileX >> 2)
                                + ((verticalOffset >> 5) << 3)];
                    } else {
                        return memory[0x5C00 + backgroundTileX
                                + ((verticalOffset >> 3) << 5)];
                    }
                }
            }
            switch (nametableMappings[(address >> 10) & 3]) {
                case 0:
                    return vram[0x2000 | offset];
                case 1:
                    return vram[0x2400 | offset];
                case 2:
                    if (extendedRamMode < 2) {
                        return memory[0x5C00 | offset];
                    } else {
                        return 0;
                    }
                default:
                    return isAttribute ? fillModeAttribute : fillModeTile;
            }
        }

        return vram[address];
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address >= 0x2000 && address < 0x3000) {
            switch (nametableMappings[(address >> 10) & 3]) {
                case 0:
                    vram[0x2000 | (address & 0x03FF)] = value;
                    break;
                case 1:
                    vram[0x2400 | (address & 0x03FF)] = value;
                    break;
                case 2:
                    memory[0x5C00 | (address & 0x03FF)] = value;
                    break;
            }
        } else {
            vram[address] = value;
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x6000) {
            final int bank = address >> prgShift;
            final int addr = prgBanks[bank] | (address & prgAddressMask);
            final int value = prgRamBanks[bank]
                    ? xram[addr & 0xFFFF]
                    : prgROM[addr & prgRomSizeMask];
            audio.updatePcmValue(address, value);
            return value;
        } else {
            final int value = audio.readRegister(address);
            if (value >= 0) {
                return value;
            } else if (address == 0x5204) {
                return readIrqStatus();
            } else if (address >= 0x5C00) {
                if (extendedRamMode < 2) {
                    return 0;
                } else {
                    return memory[address];
                }
            } else {
                return memory[address];
            }
        }
    }

    @Override
    public void writeCpuMemory(final int address, final int value) {
        super.writeCpuMemory(address, value);
        if ((address & 0xE007) == REG_PPU_MASK && (value & 0x18) == 0) {
            irqCounter = -2;
            inFrame = false;
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (audio.writeRegister(address, value)) {
            return;
        }
        switch (address) {
            case 0x5100:
                writePrgMode(value);
                break;
            case 0x5101:
                writeChrMode(value);
                break;
            case 0x5102:
                writePrgRamWriteProtect1(value);
                break;
            case 0x5103:
                writePrgRamWriteProtect2(value);
                break;
            case 0x5104:
                writeExtendedRamMode(value);
                break;
            case 0x5105:
                writeNametableMapping(value);
                break;
            case 0x5106:
                writeFillModeTile(value);
                break;
            case 0x5107:
                writeFillModeAttribute(value);
                break;
            case 0x5113:
                writePrgRamBank(value);
                break;
            case 0x5114:
                writePrgReg(0, value);
                break;
            case 0x5115:
                writePrgReg(1, value);
                break;
            case 0x5116:
                writePrgReg(2, value);
                break;
            case 0x5117:
                writePrgReg(3, value);
                break;
            case 0x5120:
            case 0x5121:
            case 0x5122:
            case 0x5123:
            case 0x5124:
            case 0x5125:
            case 0x5126:
            case 0x5127:
            case 0x5128:
            case 0x5129:
            case 0x512A:
            case 0x512B:
                writeChrReg(address & 0x000F, value);
                break;
            case 0x5130:
                writeUpperChrBits(value);
                break;
            case 0x5200:
                writeVerticalSplitMode(value);
                break;
            case 0x5201:
                writeVerticalSplitScroll(value);
                break;
            case 0x5202:
                writeVerticalSplitBank(value);
                break;
            case 0x5203:
                writeIrqScanline(value);
                break;
            case 0x5204:
                writeIrqStatus(value);
                break;
            default:
                writeToBanks(address, value);
                break;
        }
    }

    private void writeToBanks(final int address, final int value) {
        if (address < 0x6000) {
            if (address >= 0x5C00) {
                if (extendedRamMode < 2) {
                    memory[address] = inFrame ? value : 0;
                } else if (extendedRamMode == 2) {
                    memory[address] = value;
                }
            } else {
                memory[address] = value;
            }
        } else if (prgRamWriteEnabled) {
            final int bank = address >> prgShift;
            if (prgRamBanks[bank]) {
                xram[(prgBanks[bank] | (address & prgAddressMask)) & 0xFFFF] = value;
            }
        }
    }

    private void writePrgMode(final int value) {
        prgMode = value & 3;
        updatePrgBanks();
    }

    private void writeChrMode(final int value) {
        chrMode = value & 3;
        updateChrBanks();
    }

    private void writePrgRamWriteProtect1(final int value) {
        prgRamWriteProtect1 = (value & 3) == 2;
        updatePrgRamWriteProtection();
    }

    private void writePrgRamWriteProtect2(final int value) {
        prgRamWriteProtect2 = (value & 3) == 1;
        updatePrgRamWriteProtection();
    }

    private void writeExtendedRamMode(final int value) {
        extendedRamMode = value & 3;
    }

    private void writeNametableMapping(final int value) {
        for (int i = 3; i >= 0; i--) {
            nametableMappings[i] = (value >> (i << 1)) & 3;
        }
    }

    private void writeFillModeTile(final int value) {
        fillModeTile = value;
    }

    private void writeFillModeAttribute(final int value) {
        fillModeAttribute = value & 3;
        fillModeAttribute |= fillModeAttribute << 2;
        fillModeAttribute |= fillModeAttribute << 4;
    }

    private void writePrgRamBank(final int value) {
        setPrgBank(3, value & 7);
    }

    private void writePrgReg(final int register, final int value) {
        prgRegs[register] = value;
        updatePrgBanks();
    }

    private void writeChrReg(final int register, final int value) {
        chrRegs[register] = value;
        useLowerChrRegs = register < 8;
        updateChrBanks();
    }

    private void writeUpperChrBits(final int value) {
        upperChrBits = value & 3;
        updateChrBanks();
    }

    private void writeVerticalSplitMode(final int value) {
        verticalSplitEnabled = getBitBool(value, 7);
        verticalSplitRightSide = getBitBool(value, 6);
        verticalSplitTile = value & 0x1F;
    }

    private void writeVerticalSplitScroll(final int value) {
        verticalSplitScrollValue = value;
    }

    private void writeVerticalSplitBank(final int value) {
        verticalSplitBank = value << 12;
    }

    private void writeIrqScanline(final int value) {
        irqScanline = value;
    }

    private void writeIrqStatus(final int value) {
        irqEnabled = getBitBool(value, 7);
//    irq = irqEnabled && irqPending;
        updateIrq();
    }

    private int readIrqStatus() {
        int value = 0;
        if (irqPending) {
            value |= 0x80;
        }
        if (inFrame) {
            value |= 0x40;
        }
        irqPending = false;
//    if (irqEnabled) {
//      irq = false;
        updateIrq();
//    }
        return value;
    }

    private void updatePrgBanks() {
        switch (prgMode) {
            case 0: {
                int bank4 = setPrg(4, 3, 0x7C);
                setPrgBank(5, bank4 | 0x01);
                setPrgBank(6, bank4 | 0x02);
                setPrgBank(7, bank4 | 0x03);
                prgRamBanks[5] = prgRamBanks[6] = prgRamBanks[7] = false;
                break;
            }
            case 1:
                setPrgBank(5, setPrg(4, 1, 0x7E) | 0x01);
                prgRamBanks[5] = prgRamBanks[4];
                setPrgBank(7, setPrg(6, 3, 0x7E) | 0x01);
                prgRamBanks[7] = false;
                break;
            case 2:
                setPrgBank(5, setPrg(4, 1, 0x7E) | 0x01);
                prgRamBanks[5] = prgRamBanks[4];
                setPrg(6, 2);
                setPrg(7, 3);
                break;
            case 3:
                setPrg(4, 0);
                setPrg(5, 1);
                setPrg(6, 2);
                setPrg(7, 3);
                break;
        }
    }

    private int setPrg(final int bank, final int register) {
        return setPrg(bank, register, 0x7F);
    }

    private int setPrg(final int bank, final int register, final int mask) {
        int value = prgRegs[register] & mask;
        prgRamBanks[bank] = register != 3 && !getBitBool(prgRegs[register], 7);
        if (prgRamBanks[bank]) {
            value &= 0x07;
        }
        setPrgBank(bank, value);
        return value;
    }

    private void updateChrBanks() {
        if (inFrame && ppu.isSpriteSize8x16()) {
            System.arraycopy(chrRegs, 0, cRegs[0], 0, 8);
            System.arraycopy(chrRegs, 8, cRegs[1], 0, 4);
            System.arraycopy(chrRegs, 8, cRegs[1], 4, 4);
        } else if (useLowerChrRegs) {
            System.arraycopy(chrRegs, 0, cRegs[0], 0, 8);
            System.arraycopy(chrRegs, 0, cRegs[1], 0, 8);
        } else {
            System.arraycopy(chrRegs, 8, cRegs[0], 0, 4);
            System.arraycopy(chrRegs, 8, cRegs[0], 4, 4);
            System.arraycopy(chrRegs, 8, cRegs[1], 0, 4);
            System.arraycopy(chrRegs, 8, cRegs[1], 4, 4);
        }
        final int upper = upperChrBits << 8;
        for (int i = 1; i >= 0; i--) {
            switch (chrMode) {
                case 0: {
                    int bank = (upper | cRegs[i][7]) << 3;
                    for (int j = 7; j >= 0; j--) {
                        setChrBank(i, j, bank | j);
                    }
                    break;
                }
                case 1:
                    for (int j = 7; j >= 0; j -= 4) {
                        int bank = (upper | cRegs[i][j]) << 2;
                        setChrBank(i, j - 3, bank);
                        setChrBank(i, j - 2, bank | 0x01);
                        setChrBank(i, j - 1, bank | 0x02);
                        setChrBank(i, j, bank | 0x03);
                    }
                    break;
                case 2:
                    for (int j = 7; j >= 0; j -= 2) {
                        int bank = (upper | cRegs[i][j]) << 1;
                        setChrBank(i, j - 1, bank);
                        setChrBank(i, j, bank | 0x01);
                    }
                    break;
                case 3:
                    for (int j = 7; j >= 0; j--) {
                        setChrBank(i, j, upper | cRegs[i][j]);
                    }
                    break;
            }
        }
    }

    private void setChrBank(final int index, final int bank, final int value) {
        cBanks[index][bank] = value << chrShift;
    }

    private void updatePrgRamWriteProtection() {
        prgRamWriteEnabled = prgRamWriteProtect1 && prgRamWriteProtect2;
    }

    void updateIrq() {
        cpu.interrupt().setMapperIrq((irqEnabled && irqPending) || audio.isIrq());
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (scanline == 240 && scanlineCycle == 0) {
            irqCounter = -2;
            inFrame = false;
            updateChrBanks();
        }
        if (rendering) {
            switch (scanlineCycle) {
                case 0:
                    switch (scanline) {
                        case 0:
                            irqPending = false;
                            break;
                        case 1:
                            inFrame = true;
                            break;
                    }
                    irqCounter++;
                    if (irqScanline != 0 && irqCounter == irqScanline) {
                        irqPending = true;
                    }
                    updateIrq();
                    break;
                case 256:
                    chrBanks = cBanks[0];
                    spriteTile = true;
                    updateChrBanks();
                    break;
                case 320:
                    chrBanks = cBanks[1];
                    spriteTile = false;
                    backgroundTileX = -1;
                    backgroundTileSkip = 2;
                    if (scanline == -1) {
                        verticalOffset = verticalSplitScrollValue;
                        if (verticalOffset >= 240) {
                            verticalOffset -= 16;
                        }
                    } else if (scanline < 240) {
                        verticalOffset++;
                    }
                    if (verticalOffset >= 240) {
                        verticalOffset -= 240;
                    }
                    updateChrBanks();
                    break;
            }
        }
    }

    @Override
    public void update() {
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
