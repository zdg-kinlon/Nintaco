package nintaco.mappers.frontfareast;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.ONE_SCREEN_A;
import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class FrontFareast extends Mapper {

    private static final long serialVersionUID = 0;

    protected int irqCounter;
    protected boolean irqEnabled;
    protected boolean ffeAltMode;

    public FrontFareast(final CartFile cartFile) {
        super(cartFile, 8, 8);
        xram = chrRamPresent ? new int[0x8000] : null;
    }

    @Override
    public void init() {
        irqCounter = 0;
        irqEnabled = false;
        ffeAltMode = true;
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000 && chrRamPresent) {
            xram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & 0x7FFF] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            if (chrRamPresent) {
                return xram[(chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & 0x7FFF];
            } else {
                return chrROM[(chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & chrRomSizeMask];
            }
        } else {
            return vram[address];
        }
    }

    public void update() {
        if (irqEnabled) {
            irqCounter = (irqCounter + 1) & 0xFFFF;
            if (irqCounter == 0) {
                cpu.interrupt().setMapperIrq(true);
                irqEnabled = false;
            }
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address) {
            case 0x42FE:
                ffeAltMode = !getBitBool(value, 7);
                setNametableMirroring(ONE_SCREEN_A + getBit(value, 4));
                break;

            case 0x42FF:
                setNametableMirroring(getBit(value, 4));
                break;

            case 0x4501:
                irqEnabled = false;
                cpu.interrupt().setMapperIrq(false);
                break;

            case 0x4502:
                irqCounter = (irqCounter & 0xFF00) | value;
                cpu.interrupt().setMapperIrq(false);
                break;

            case 0x4503:
                irqCounter = (irqCounter & 0x00FF) | (value << 8);
                irqEnabled = true;
                cpu.interrupt().setMapperIrq(false);
                break;
        }
        super.writeMemory(address, value);
    }
}