package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper213 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper213(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank((address >> 3) & 7);
        setPrgBank((address >> 1) & 3);
    }
}