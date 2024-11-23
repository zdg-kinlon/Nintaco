package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class AC08 extends Mapper {

    private static final long serialVersionUID = 0;

    public AC08(final CartFile cartFile) {
        super(cartFile, 8, 0, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBank(4, -4);
        setPrgBank(5, -3);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address == 0x4025) {
            setNametableMirroring(getBit(value, 3));
        } else if (address >= 0x8000) {
            final int bank3;
            if (address == 0x8001) {
                bank3 = (value >> 1) & 0x0F;
            } else {
                bank3 = value & 0x0F;
            }
            setPrgBank(3, bank3);
        } else {
            super.writeMemory(address, value);
        }
    }
}
