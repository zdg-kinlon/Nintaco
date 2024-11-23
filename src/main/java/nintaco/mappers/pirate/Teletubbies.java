package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

// TODO PARTIAL IMPLEMENTATION, NO TEST ROM

public class Teletubbies extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean locked;
    private int BBB000;

    public Teletubbies(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        int BBBbbb;
        if (locked) {
            BBBbbb = BBB000 | (value & 0x07);
        } else {
            locked = getBitBool(address, 1);
            BBBbbb = ((address & 0x0004) << 3) | (value & 0x1F);
            BBB000 = BBBbbb & 0x38;
        }
        setNametableMirroring(getBit(value, 5) ^ 1);
        switch ((address >> 6) & 3) {
            case 0:
                setPrgBank(2, BBBbbb);
                setPrgBank(3, BBBbbb | 0x07);
                break;
            case 1:
                setPrgBank(2, BBBbbb & 0x3E);
                setPrgBank(3, BBBbbb | 0x07);
                break;
            case 2:
                setPrgBank(2, BBBbbb);
                setPrgBank(3, BBBbbb);
                break;
            case 3:
                setPrgBank(2, BBBbbb & 0x3E);
                setPrgBank(3, BBBbbb | 0x01);
                break;
        }
    }
}
