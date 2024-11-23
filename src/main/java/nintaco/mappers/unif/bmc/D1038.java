package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class D1038 extends Mapper {

    private static final long serialVersionUID = 0;

    private int register;
    private int dipSwitch;

    public D1038(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void resetting() {
        dipSwitch = (dipSwitch + 1) & 3;
        init();
    }

    private void updateBanks() {
        if (getBitBool(register, 7)) {
            final int bank = (register & 0x70) >> 4;
            setPrgBank(2, bank);
            setPrgBank(3, bank);
        } else {
            final int b = (register & 0x60) >> 4;
            setPrgBank(2, b);
            setPrgBank(3, b | 1);
        }
        setChrBank(register & 7);
        setNametableMirroring(getBit(register, 3));
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x6000 && getBitBool(register, 8)) {
            return dipSwitch;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        register = address;
        updateBanks();
    }
}
