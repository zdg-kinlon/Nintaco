package nintaco.mappers.kaiser;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Kaiser7031 extends Mapper {

    private static final long serialVersionUID = 0;

    public Kaiser7031(final CartFile cartFile) {
        super(cartFile, 32, 0, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        for (int i = 15; i >= 0; i--) {
            setPrgBank(16 + i, 15 - i);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(0x0C | ((address >> 11) & 3), value);
    }
}
