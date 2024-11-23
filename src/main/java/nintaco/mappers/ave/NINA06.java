package nintaco.mappers.ave;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class NINA06 extends Mapper {

    private static final long serialVersionUID = 0;

    public NINA06(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x4100 && address < 0x6000 && (address & 0x5100) != 0) {
            setPrgBank((value >> 3) & 0x07);
            setChrBank((value & 0x07) | ((value >> 3) & 0x08));
            setNametableMirroring(getBit(value, 7) ^ 1);
        }
    }
}
