package nintaco.mappers.ave;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class NINA001 extends Mapper {

    private static final long serialVersionUID = 0;

    public NINA001(final CartFile cartFile) {
        super(cartFile, 2, 2, 0x7FFD, 0x8000);
    }

    @Override
    public void init() {
        setPrgBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address) {
            case 0x7FFD:
                setPrgBank(value & 1);
                break;
            case 0x7FFE:
                setChrBank(0, value & 0x0F);
                break;
            case 0x7FFF:
                setChrBank(1, value & 0x0F);
                break;
        }
    }
}
