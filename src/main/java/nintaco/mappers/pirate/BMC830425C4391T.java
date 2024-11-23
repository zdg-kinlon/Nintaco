package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.VERTICAL;
import static nintaco.util.BitUtil.getBitBool;

public class BMC830425C4391T extends Mapper {

    private static final long serialVersionUID = 0;

    private int innerBank;
    private int outerBank;

    public BMC830425C4391T(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        innerBank = outerBank = 0;
        setNametableMirroring(VERTICAL);
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        final int outer = outerBank & 0x7F;
        final int size = getBitBool(outerBank, 7) ? 0x07 : 0x0F;
        setPrgBank(2, outer | (innerBank & size));
        setPrgBank(3, outer | size);
    }

    private void writeInnerBank(final int value) {
        innerBank = value;
        updateState();
    }

    private void writeOuterBank(final int address) {
        outerBank = (address & 0x00FF) << 3;
        updateState();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        writeInnerBank(value);
        if ((address & 0xFFE0) == 0xF0E0) {
            writeOuterBank(address);
        }
    }
}