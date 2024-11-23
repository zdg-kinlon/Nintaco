package nintaco.mappers.pirate;

import nintaco.files.NesFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper061 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper061(NesFile nesFile) {
        super(nesFile, 4, 0);
    }

    @Override
    protected void writeRegister(int address, int value) {
        int bank = ((address & 0x000F) << 1) | getBit(address, 5);
        if (getBitBool(address, 4)) {
            setPrgBank(2, bank);
            setPrgBank(3, bank);
        } else {
            setPrgBank(2, bank & 0x1E);
            setPrgBank(3, bank | 0x01);
        }
        setNametableMirroring(getBit(address, 7));
    }
}
