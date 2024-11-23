package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

public class LH32 extends Mapper {

    private static final long serialVersionUID = 0;

    public LH32(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x6000, 0x6000);
    }

    @Override
    public void init() {
        setChrBank(0);

        setPrgBank(3, 0);
        setPrgBank(4, -4);
        setPrgBank(5, -3);
        setPrgBank(7, -1);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0xC000) {
            return memory[address];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address == 0x6000) {
            setPrgBank(3, value);
        }
    }
}