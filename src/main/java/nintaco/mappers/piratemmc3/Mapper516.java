package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper516 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper516(final CartFile cartFile) {
        super(cartFile);
    }

    private void writeOuterBankRegister(final int address) {
        final int bank = address & 0x000F;
        setPrgBlock((bank & 0x3) << 4, 0x0F);
        setChrBlock((bank & 0xC) << 5, 0x7F);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if (getBitBool(address, 4)) {
            writeOuterBankRegister(address);
        }
        super.writeRegister(address, value);
    }
}