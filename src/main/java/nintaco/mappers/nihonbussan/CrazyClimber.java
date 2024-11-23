package nintaco.mappers.nihonbussan;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class CrazyClimber extends Mapper {

    private static final long serialVersionUID = 0;

    public CrazyClimber(final CartFile cartFile) {
        super(cartFile, 4, 0);
        setPrgBank(2, 0);
    }

    @Override
    protected void writeRegister(int address, int value) {
        setPrgBank(3, value & 7);
    }
}
