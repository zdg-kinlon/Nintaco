package nintaco.mappers.pirate;

import nintaco.files.CartFile;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper225 extends Mapper062 {

    private static final long serialVersionUID = 0;

    public Mapper225(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        final int H = getBit(address, 14) << 7;
        writePrgBanks(((address >> 6) & 0x3F) | H, getBitBool(address, 12));
        setChrBank(0, (address & 0x003F) | H);
        setNametableMirroring(getBit(address, 13));
    }
}
