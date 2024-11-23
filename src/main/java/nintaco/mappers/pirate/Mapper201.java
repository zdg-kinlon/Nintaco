package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper201 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper201(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        setChrBank(address & 3);
        setPrgBank(address & 3);
    }
}
