package nintaco.mappers.irem;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.*;

public class TAMS1 extends Mapper {

    private static final long serialVersionUID = 0;

    public TAMS1(final CartFile cartFile) {
        super(cartFile, 4, 0);
        setPrgBank(2, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(3, value & 0x0F);
        writeNametableMirroring(value);
    }

    private void writeNametableMirroring(final int value) {
        switch (value >> 6) {
            case 0:
                setNametableMirroring(ONE_SCREEN_A);
                break;
            case 1:
                setNametableMirroring(HORIZONTAL);
                break;
            case 2:
                setNametableMirroring(VERTICAL);
                break;
            case 3:
                setNametableMirroring(ONE_SCREEN_B);
                break;
        }
    }
}
