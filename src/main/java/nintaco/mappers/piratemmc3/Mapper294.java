package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper294 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int outerBank;

    public Mapper294(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        outerBank = 0;
        setNametableMirroring(NametableMirroring.HORIZONTAL);
        updateState();
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        if (getBitBool(outerBank, 2)) {
            setPrgBlock(outerBank & 0xF0, 0x0F);
            setChrBlock((outerBank & 0xF0) << 3, 0x7F);
        } else {
            setPrgBlock(outerBank & 0xE0, 0x1F);
            setChrBlock(0, 0xFF);
        }
    }

    private void writeOuterBank(final int value) {
        outerBank = value;
        updateState();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address == 0x6001) {
            writeOuterBank(value);
        }
        super.writeMemory(address, value);
    }
}
