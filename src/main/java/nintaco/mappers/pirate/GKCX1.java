package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class GKCX1 extends Mapper {

    private static final long serialVersionUID = 0;

    public GKCX1(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank((address >> 3) & 3);
        setChrBank(address & 7);
    }
}
