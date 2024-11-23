package nintaco.mappers.sachen;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class SA0036 extends CNROM {

    private static final long serialVersionUID = 0;

    public SA0036(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        setChrBank(0, getBit(value, 7));
    }
}
