package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

public class BMCK3046 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMCK3046(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, 7);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int outer = value & 0x38;
        setPrgBank(2, outer | (value & 7));
        setPrgBank(3, outer | 7);
    }
}