package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper058 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper058(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        chrBanks[0] = (address & 0x0038) << 10;
        setNametableMirroring(getBit(address, 7));
        final int prgBank = address & 7;
        if (getBitBool(address, 6)) {
            setPrgBank(2, prgBank);
            setPrgBank(3, prgBank);
        } else {
            prgBanks[2] = (prgBank & 6) << 15;
            prgBanks[3] = prgBanks[2] | 0x4000;
        }
    }
}
