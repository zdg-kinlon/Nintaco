package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper062 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper062(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        writePrgBanks(((address >> 8) & 0x3F) | (address & 0x0040),
                getBitBool(address, 5));
        setChrBank(0, ((address & 0x001F) << 2) | (value & 0x03));
        setNametableMirroring(getBit(address, 7));
    }

    protected void writePrgBanks(int prgBank, boolean prgMode1) {
        if (prgMode1) {
            setPrgBank(2, prgBank);
            setPrgBank(3, prgBank);
        } else {
            setPrgBank(2, prgBank & 0xFE);
            setPrgBank(3, prgBank | 0x01);
        }
    }
}
