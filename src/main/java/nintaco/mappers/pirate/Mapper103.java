package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper103 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean prgRamDisabled;

    public Mapper103(final CartFile cartFile) {
        super(cartFile, 8, 1);
    }

    @Override
    public void init() {
        setChrBank(0);
        setPrgBanks(4, 4, -4);
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x6000 && (prgRamDisabled || address >= 0xD800
                || (address >= 0x8000 && address < 0xB800))) {
            return prgROM[(prgBanks[address >> prgShift] | (address & prgAddressMask))
                    & prgRomSizeMask];
        } else {
            return memory[address];
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
                setPrgBank(3, value & 0x0F);
                break;
            case 0xE000:
                setNametableMirroring(getBit(value, 3));
                break;
            case 0xF000:
                prgRamDisabled = getBitBool(value, 4);
                break;
        }
    }
}