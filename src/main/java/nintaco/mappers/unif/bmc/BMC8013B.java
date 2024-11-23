package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class BMC8013B extends Mapper {

    private static final long serialVersionUID = 0;

    private int biosPrg;
    private int romPrg;
    private boolean romMode;

    public BMC8013B(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        biosPrg = romPrg = 0;
        romMode = false;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        if (romMode) {
            setPrgBank(2, (romPrg & 0x70) | (biosPrg & 0x0F));
        } else {
            setPrgBank(2, biosPrg & 0x03);
        }
        setPrgBank(3, romPrg & 0x7F);

        setNametableMirroring(((biosPrg >> 4) & 1));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int reg = (address >> 13) & 0x03;
        if (reg == 0) {
            biosPrg = value;
        } else {
            romPrg = value;
            romMode = getBitBool(reg, 1);
        }
        updateState();
    }
}