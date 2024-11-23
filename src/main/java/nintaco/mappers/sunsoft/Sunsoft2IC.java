package nintaco.mappers.sunsoft;

import nintaco.files.*;
import nintaco.mappers.*;

public class Sunsoft2IC extends Mapper {

    private static final long serialVersionUID = 0;

    public Sunsoft2IC(final CartFile cartFile) {
        super(cartFile, 4, 0);
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        prgBanks[2] = (value & 0x70) << 10;
    }
}
