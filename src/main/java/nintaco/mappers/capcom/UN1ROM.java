package nintaco.mappers.capcom;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class UN1ROM extends Mapper {

    private static final long serialVersionUID = 0;

    public UN1ROM(final CartFile cartFile) {
        super(cartFile, 4, 0);
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        prgBanks[2] = (value & 0x1C) << 12;
    }
}
