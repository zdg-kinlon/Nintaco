package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class BMC810544CA1 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMC810544CA1(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        int bank = address >> 7;
        if (getBitBool(address, 6)) {
            bank <<= 1;
            setPrgBank(2, bank);
            setPrgBank(3, bank | 1);
        } else {
            bank = (bank << 1) | ((address >> 5) & 1);
            setPrgBank(2, bank);
            setPrgBank(3, bank);
        }
        setChrBank(address & 0x0F);
        setNametableMirroring(getBit(address, 4));
    }
}

