package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class BMCTJ03 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMCTJ03(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = (address >> 8) & 7;
        setPrgBank(bank);
        setChrBank(bank);
        setNametableMirroring(getBit(address, 1));
    }
}
