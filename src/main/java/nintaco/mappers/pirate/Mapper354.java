package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper354 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean chrRamWritesEnabled;

    public Mapper354(final CartFile cartFile) {
        super(cartFile, 8, 1, 0xF000, 0x6000);
    }

    @Override
    public void init() {
        chrRamWritesEnabled = true;
        writeRegister(0xF000, 0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = (value & 0x3F) << 1;
        final int lowBit = getBit(value, 7);
        switch (address & 7) {
            case 0:
            case 4:
                set4PrgBanks(4, bank & 0x7C);
                break;
            case 1:
                set2PrgBanks(4, bank);
                set2PrgBanks(6, bank | 0x0E);
                break;
            case 2:
            case 6:
                setPrgBank(4, bank | lowBit);
                setPrgBank(5, bank | lowBit);
                setPrgBank(6, bank | lowBit);
                setPrgBank(7, bank | lowBit);
                break;
            case 3:
            case 7:
                setPrgBank(4, bank);
                setPrgBank(5, bank | 1);
                setPrgBank(6, bank);
                setPrgBank(7, bank | 1);
                break;
            case 5:
                setPrgBank(3, bank | lowBit);
                set4PrgBanks(4, (bank & 0xF0) | 0x0C);
                break;
        }
        chrRamWritesEnabled = !getBitBool(address, 3);
        setNametableMirroring(getBit(value, 6));
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (chrRamWritesEnabled || address >= 0x2000) {
            vram[address] = value;
        }
    }
}