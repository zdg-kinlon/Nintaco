package nintaco.mappers.sachen;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class SA0037 extends GxROM {

    private static final long serialVersionUID = 0;

    public SA0037(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        prgBanks[1] = (value & 0x08) << 12;
        setChrBank(value & 0x07);
    }
}
