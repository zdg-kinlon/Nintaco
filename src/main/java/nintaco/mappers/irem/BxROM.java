package nintaco.mappers.irem;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class BxROM extends Mapper {

    private static final long serialVersionUID = 0;

    public BxROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(value);
    }
}
