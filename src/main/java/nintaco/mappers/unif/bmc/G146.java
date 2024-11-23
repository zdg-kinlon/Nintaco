package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class G146 extends Mapper {

    private static final long serialVersionUID = 0;

    public G146(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (getBitBool(address, 11)) {
            setPrgBank(2, (address & 0x1F) | (address & ((address & 0x40) >> 6)));
            setPrgBank(3, (address & 0x18) | 7);
        } else {
            if (getBitBool(address, 6)) {
                setPrgBank(2, address & 0x1F);
                setPrgBank(3, address & 0x1F);
            } else {
                setPrgBank(2, address & 0x1E);
                setPrgBank(3, (address & 0x1E) | 1);
            }
        }
        setNametableMirroring(getBit(address, 7));
    }
}
