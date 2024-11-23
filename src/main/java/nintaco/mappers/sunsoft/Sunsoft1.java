package nintaco.mappers.sunsoft;

import nintaco.files.*;
import nintaco.mappers.*;

public class Sunsoft1 extends Mapper {

    private static final long serialVersionUID = 0;

    public Sunsoft1(final CartFile cartFile) {
        super(cartFile, 0, 2);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x6000 && address < 0x8000) {
            setChrBank(0, value & 0x07);
            chrBanks[1] = ((value & 0x30) | 0x40) << 8;
        } else {
            memory[address] = value;
        }
    }
}
