package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class Mapper200 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper200(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = address & 7;
        setChrBank(0, bank);
        setPrgBank(2, bank);
        setPrgBank(3, bank);
        setNametableMirroring(getBit(address, 3));
    }
}
