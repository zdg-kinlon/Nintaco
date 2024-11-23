package nintaco.mappers.jy;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.MMC3;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper364 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper364(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBankRegister(0);
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeOuterBankRegister(final int value) {
        setPrgBlock((value & 0x40) >> 1, getBitBool(value, 5) ? 0x0F : 0x1F);
        setChrBlock((value & 0x10) << 4, getBitBool(value, 5) ? 0x7F : 0xFF);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF000) == 0x7000) {
            writeOuterBankRegister(value);
        }
        super.writeMemory(address, value);
    }
}
