package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class Mapper204 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper204(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bitMask = address & 0x06;
        final int bank = bitMask + ((bitMask == 0x06) ? 0 : (address & 1));
        setPrgBank(2, bank);
        setPrgBank(3, bitMask + ((bitMask == 0x06) ? 1 : (address & 1)));
        setChrBank(bank);
        setNametableMirroring(getBit(address, 4));
    }
}