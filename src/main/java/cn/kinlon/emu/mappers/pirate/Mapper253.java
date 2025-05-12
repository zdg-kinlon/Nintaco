package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper253 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] chrLow = new int[8];
    private final int[] chrHigh = new int[8];
    private final boolean[] chrRamBanks = new boolean[8];

    private boolean chrRomMode;

    private int irqScaler;
    private int irqReloadValue;
    private int irqCounter;
    private boolean irqEnabled;

    public Mapper253(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            return (chrRamBanks[address >> chrShift] ? vram : chrROM)
                    [(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & chrRomSizeMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000 && chrAddressMask != 0) {
            if (chrRamBanks[address >> chrShift]) {
                vram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                        & chrRomSizeMask] = value;
            }
        } else {
            vram[address] = value;
        }
    }

    private void updateChrBanks() {
        for (int i = 7; i >= 0; i--) {
            final int low = chrLow[i];
            final int bank = (chrHigh[i] << 8) | low;
            if ((low == 4 || low == 5) && !chrRomMode) {
                chrRamBanks[i] = true;
                setChrBank(i, bank & 1);
            } else {
                chrRamBanks[i] = false;
                setChrBank(i, bank);
            }
        }
    }

    @Override
    public void update() {
        if (irqEnabled) {
            irqScaler = (irqScaler + 1) & 0xFFFF;
            if (irqScaler >= 114) {
                irqScaler = 0;
                irqCounter = (irqCounter + 1) & 0xFF;
                if (irqCounter == 0) {
                    irqCounter = irqReloadValue;
                    cpu.setMapperIrq(true);
                }
            }
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        if (address >= 0xB000 && address <= 0xE00C) {
            final int index = ((((address & 0x08) | (address >> 8)) >> 3) + 2) & 0x07;
            final int shift = address & 0x04;
            final int low = (chrLow[index] & (0xF0 >> shift)) | (value << shift);
            chrLow[index] = low;
            if (index == 0) {
                if (low == 0xC8) {
                    chrRomMode = false;
                } else if (low == 0x88) {
                    chrRomMode = true;
                }
            }
            if (shift != 0) {
                chrHigh[index] = value >> 4;
            }
            updateChrBanks();
        } else {
            switch (address) {
                case 0x8010:
                    setPrgBank(4, value);
                    break;
                case 0xA010:
                    setPrgBank(5, value);
                    break;
                case 0x9400:
                    setNametableMirroring(value & 3);
                    break;
                case 0xF000:
                    irqReloadValue = (irqReloadValue & 0xF0) | (value & 0x0F);
                    cpu.setMapperIrq(false);
                    break;
                case 0xF004:
                    irqReloadValue = (irqReloadValue & 0x0F) | (value << 4);
                    cpu.setMapperIrq(false);
                    break;
                case 0xF008:
                    irqCounter = irqReloadValue;
                    irqEnabled = getBitBool(value, 1);
                    irqScaler = 0;
                    cpu.setMapperIrq(false);
                    break;
            }
        }
    }
}