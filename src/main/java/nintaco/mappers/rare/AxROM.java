package nintaco.mappers.rare;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.*;

public class AxROM extends Mapper {

    private static final long serialVersionUID = 0;

    public AxROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(value & 0x0F);
        setNametableMirroring(ONE_SCREEN_A + getBit(value, 4));
    }
}
