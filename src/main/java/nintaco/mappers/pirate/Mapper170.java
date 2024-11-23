package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper170 extends Mapper {

    private static final long serialVersionUID = 0;

    private int register;

    public Mapper170(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        register = 0;
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x7001 || address == 0x7777) {
            return ((address >> 8) & 0x7F) | register;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address == 0x6502 || address == 0x7000) {
            register = (value << 1) & 0x80;
        }
    }
}
