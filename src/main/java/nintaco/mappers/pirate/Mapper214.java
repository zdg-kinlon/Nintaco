package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper214 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper214(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank(address & 3);

        final int bank = (address >> 2) & 3;
        setPrgBank(2, bank);
        setPrgBank(3, bank);
    }
}