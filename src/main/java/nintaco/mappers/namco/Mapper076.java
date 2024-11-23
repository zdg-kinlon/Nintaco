package nintaco.mappers.namco;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper076 extends Mapper {

    private static final long serialVersionUID = 0;

    private int register;

    public Mapper076(final CartFile cartFile) {
        super(cartFile, 8, 4);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        if ((address & 0x8001) == 0x8000) {
            register = value & 7;
        } else {
            value &= 0x3F;
            if (register >= 6) {
                setPrgBank(register - 2, value);
            } else if (register >= 2) {
                setChrBank(register - 2, value);
            }
        }
    }
}
