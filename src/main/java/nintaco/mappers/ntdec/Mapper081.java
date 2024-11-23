package nintaco.mappers.ntdec;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper081 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper081(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(2, (address >> 2) & 3);
        setChrBank(address & 3);
    }
}
