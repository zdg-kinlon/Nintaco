package nintaco.mappers.jy;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.MMC3;

import static nintaco.mappers.NametableMirroring.FOUR_SCREEN;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper356 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int prgRomOffset;
    private int prgRomMask;
    private int chrRomOffset;
    private int chrRomMask;
    private int regNum;
    private int prgMask;
    private int prgOffset;
    private int chrMask;
    private int chrOffset;
    private int mirroring;
    private boolean locked;
    private boolean chrRomMode;
    private boolean vramMode;

    public Mapper356(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        prgRomOffset = chrRomOffset = prgOffset = chrOffset = regNum = 0;
        prgRomMask = chrRomMask = -1;
        prgMask = 0x3F;
        chrMask = 0xFF;
        locked = chrRomMode = vramMode = false;
        updateState();
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        setPrgBlock(prgOffset, prgMask);
        if (chrRomMode) {
            setChrBlock(chrOffset, chrMask);
            chrRamPresent = false;
        } else {
            setChrBlock(0, -1);
            chrRamPresent = true;
        }
        if (vramMode) {
            setNametableMirroring(FOUR_SCREEN);
        } else {
            super.writeMirroring(mirroring);
        }
        chrRomOffset = chrOffset << 10;
        prgRomMask = ((prgMask + 1) << 13) - 1;
        chrRomMask = ((chrMask + 1) << 10) - 1;
        updateBanks();
    }

    private void writeReg(final int value) {
        if (!locked) {
            switch (regNum++ & 3) {
                case 0:
                    chrOffset = (chrOffset & ~0xFF) | value;
                    break;
                case 1:
                    prgOffset = (prgOffset & ~0xFF) | value;
                    prgRomOffset = prgOffset << 13;
                    break;
                case 2:
                    chrMask = 0xFF >> (~value & 0x0F);
                    chrOffset = (chrOffset & 0xFF) | ((value & 0xF0) << 4);
                    prgOffset = (prgOffset & 0xFF) | ((value & 0xC0) << 2);
                    chrRomMode = getBitBool(value, 5);
                    vramMode = getBitBool(value, 6);
                    break;
                case 3:
                    prgMask = ~value & 0x3F;
                    locked = getBitBool(value, 6);
                    break;
            }
            updateState();
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            writeReg(value);
        }
        super.writeMemory(address, value);
    }

    @Override
    public int readMemory(final int address) {
        if (address >= minRomAddress) {
            return prgROM[(prgRomOffset | ((prgBanks[address >> prgShift]
                    | (address & prgAddressMask)) & prgRomMask)) & prgRomSizeMask];
        } else {
            return memory[address];
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            if (chrRamPresent) {
                return (xChrRam != null ? xChrRam : vram)[(chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & chrRamSizeMask];
            } else {
                return chrROM[(chrRomOffset | ((chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & chrRomMask)) & chrRomSizeMask];
            }
        } else {
            return vram[address];
        }
    }

    @Override
    protected void writeMirroring(int value) {
        mirroring = value;
        updateState();
    }
}