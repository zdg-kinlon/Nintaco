package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper212 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper212(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE010) == 0x6000) {
            return 0x80 | super.readMemory(address);
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (getBitBool(address, 14)) {
            setPrgBanks(2, 2, address & 0x06);
        } else {
            setPrgBank(2, address & 7);
            setPrgBank(3, address & 7);
        }
        setChrBank(address & 7);
        setNametableMirroring(getBit(address, 3));
    }
}
