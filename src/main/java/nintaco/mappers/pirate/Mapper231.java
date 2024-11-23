package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class Mapper231 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper231(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    protected void writeRegister(int address, int value) {
        setNametableMirroring(getBit(address, 7));
        int bank = (address & 0x001E) | getBit(address, 5);
        setPrgBank(2, bank & 0x1E);
        setPrgBank(3, bank);
    }
}
