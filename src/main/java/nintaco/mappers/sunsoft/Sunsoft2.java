package nintaco.mappers.sunsoft;

import nintaco.files.*;
import nintaco.mappers.*;

public class Sunsoft2 extends Mapper {

    private static final long serialVersionUID = 0;

    public Sunsoft2(final CartFile cartFile) {
        super(cartFile, 4, 1);
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        prgBanks[2] = (0x70 & value) << 10;
        setChrBank(((0x80 & value) >> 4) | (value & 7));
        setNametableMirroring(2 + ((0x08 & value) >> 3));
    }
}
