package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper108 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper108(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBanks(4, 4, -4);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
            case 0xF000:
                setPrgBank(3, value);
                break;
        }
    }
}
