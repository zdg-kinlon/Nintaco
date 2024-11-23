package nintaco.mappers.waixing;

import nintaco.files.*;
import nintaco.mappers.konami.*;

public class Mapper252 extends VrcIrq {

    private static final long serialVersionUID = 0;

    private final int[] chrRegs = new int[8];
    private final boolean[] chrRamBanks = new boolean[8];

    public Mapper252(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        super.init();
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000 && chrAddressMask != 0) {
            final int bank = address >> chrShift;
            if (chrRamBanks[bank]) {
                vram[(chrBanks[bank] | (address & chrAddressMask)) & 0x1FFF] = value;
            }
        } else {
            vram[address] = value;
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            final int bank = address >> chrShift;
            if (chrRamBanks[bank]) {
                return vram[(chrBanks[bank] | (address & chrAddressMask)) & 0x1FFF];
            } else {
                return chrROM[(chrBanks[bank] | (address & chrAddressMask))
                        & chrRomSizeMask];
            }
        } else {
            return vram[address];
        }
    }

    private void updateChrBank(final int address, final int value) {
        final int bank = ((((address & 8) | (address >> 8)) >> 3) + 2) & 7;
        final int shift = address & 4;
        chrRegs[bank] = (chrRegs[bank] & (0xF0 >> shift))
                | ((value & 0x0F) << shift);
        if (chrRegs[bank] == 6 || chrRegs[bank] == 7) {
            chrRamBanks[bank] = true;
            setChrBank(bank, chrRegs[bank] & 1);
        } else {
            chrRamBanks[bank] = false;
            setChrBank(bank, chrRegs[bank]);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
                setPrgBank(4, value);
                break;
            case 0xA000:
                setPrgBank(5, value);
                break;
            case 0xB000:
            case 0xC000:
            case 0xD000:
            case 0xE000:
                updateChrBank(address, value);
                break;
            case 0xF000:
                switch (address & 0xF00C) {
                    case 0xF000:
                        writeIrqLatchLow(value);
                        break;
                    case 0xF004:
                        writeIrqLatchHigh(value);
                        break;
                    case 0xF008:
                        writeIrqControl(value);
                        break;
                    case 0xF00C:
                        writeIrqAcknowledge();
                        break;
                }
                break;
        }
    }
}
