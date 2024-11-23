package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.*;

public class UNL43272 extends Mapper {

    private static final long serialVersionUID = 0;

    private int lastAddress;

    public UNL43272(final CartFile cartFile) {
        super(cartFile, 2, 0);
    }

    @Override
    public void init() {
        setNametableMirroring(HORIZONTAL);
        writeRegister(0x8081, 0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(int address) {
        if (address >= 0x6000 && getBitBool(lastAddress, 10)) {
            address &= 0x00FE;
        }
        return super.readMemory(address);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        lastAddress = address;
        if ((address & 0x0081) == 0x0081) {
            setPrgBank((address & 0x38) >> 3);
        }
    }
}