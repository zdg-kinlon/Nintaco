package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper216 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper216(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x5000) {
            return 0;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(address & 0x01);
        setChrBank((address & 0x0E) >> 1);
    }
}
