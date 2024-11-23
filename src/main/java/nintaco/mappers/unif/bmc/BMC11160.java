package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

public class BMC11160 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMC11160(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = (value >> 4) & 7;
        setPrgBank(bank);
        setChrBank((bank << 2) | (value & 3));
        setNametableMirroring(((value >> 7) & 1) ^ 1);
    }
}
