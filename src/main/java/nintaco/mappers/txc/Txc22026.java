package nintaco.mappers.txc;

import nintaco.files.*;
import nintaco.mappers.*;

public class Txc22026 extends Mapper {

    private static final long serialVersionUID = 0;

    public Txc22026(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setNametableMirroring(((value >> 5) & 1) ^ 1);
        setPrgBank((value & 0x30) >> 4);
        setChrBank(value & 0x0F);
    }
}
