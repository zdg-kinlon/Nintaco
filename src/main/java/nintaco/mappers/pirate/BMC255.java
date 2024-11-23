package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class BMC255 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMC255(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int prgBit = (address & 0x1000) != 0 ? 0 : 1;
        final int bank = ((address >> 8) & 0x40) | ((address >> 6) & 0x3F);
        setPrgBank(2, bank & ~prgBit);
        setPrgBank(3, bank | prgBit);
        setChrBank(((address >> 8) & 0x40) | (address & 0x3F));
        setNametableMirroring(getBit(address, 13));
    }
}