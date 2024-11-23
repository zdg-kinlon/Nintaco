package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class Mapper229 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper229(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(int address, int value) {

        setChrBank(address & 0x00FF);

        if ((address & 0x001E) == 0) {
            setPrgBank(2, 0);
            setPrgBank(3, 1);
        } else {
            setPrgBank(2, address & 0x001F);
            setPrgBank(3, address & 0x001F);
        }
        setNametableMirroring(getBit(address, 5));
    }
}