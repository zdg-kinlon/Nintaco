package nintaco.mappers.jy;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.MMC3;

import static nintaco.util.BitUtil.getBitBool;

// 8-in-1 JY-119 multicart
public class Mapper267 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int outerBank;

    public Mapper267(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        outerBank = 0;
        writeOuterBank(0);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeOuterBank(final int value) {
        if (!getBitBool(outerBank, 7)) { // if game has not been selected
            outerBank = value;
            final int block = ((outerBank & 0x20) >> 2) | (outerBank & 0x06);
            setPrgBlock(block << 4, 0x1F); // select 256 KiB outer PRG-ROM bank
            setChrBlock(block << 6, 0x7F); // select 128 KiB outer CHR-ROM bank
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            writeOuterBank(value);
        }
        super.writeMemory(address, value);
    }
}