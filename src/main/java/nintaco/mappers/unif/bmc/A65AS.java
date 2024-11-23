package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.*;

public class A65AS extends Mapper {

    private static final long serialVersionUID = 0;

    private int register;

    public A65AS(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    private void updateBanks() {
        if (getBitBool(register, 6)) {
            final int b = register & 0x1E;
            setPrgBank(2, b);
            setPrgBank(3, b | 1);
        } else {
            setPrgBank(2, ((register & 0x30) >> 1) | (register & 7));
            setPrgBank(3, ((register & 0x30) >> 1) | 7);
        }
        setChrBank(0);
        if (getBitBool(register, 7)) {
            setNametableMirroring(ONE_SCREEN_A + getBit(register, 5));
        } else {
            setNametableMirroring(getBit(register, 3));
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        register = value;
        updateBanks();
    }
}
