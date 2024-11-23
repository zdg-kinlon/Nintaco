package nintaco.mappers.pirate;

import nintaco.files.NesFile;
import nintaco.mappers.Mapper;

public class Mapper203 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper203(NesFile nesFile) {
        super(nesFile, 4, 1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        setChrBank(0, value & 3);
        prgBanks[2] = prgBanks[3] = (value & 0xFC) << 12;
    }
}
