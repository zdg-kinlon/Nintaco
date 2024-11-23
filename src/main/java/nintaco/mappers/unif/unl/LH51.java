package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class LH51 extends Mapper {

    private static final long serialVersionUID = 0;

    public LH51(final CartFile cartFile) {
        super(cartFile, 8, 1);
    }

    @Override
    public void init() {
        setPrgBank(4, 0x00);
        setPrgBank(5, 0x0D);
        setPrgBank(6, 0x0E);
        setPrgBank(7, 0x0F);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xE000) {
            case 0x8000:
                setPrgBank(4, value & 0x0F);
                break;
            case 0xE000:
                setNametableMirroring(getBit(value, 3));
                break;
        }
    }
}