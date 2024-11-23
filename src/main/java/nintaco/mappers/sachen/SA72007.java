package nintaco.mappers.sachen;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class SA72007 extends CNROM {

    private static final long serialVersionUID = 0;

    public SA72007(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xE100) == 0x4100) {
            setChrBank(0, getBit(value, 7));
        }
    }
}
