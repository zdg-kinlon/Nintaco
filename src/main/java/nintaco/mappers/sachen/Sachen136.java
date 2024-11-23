package nintaco.mappers.sachen;

import nintaco.files.*;
import nintaco.mappers.txc.*;

public class Sachen136 extends JV001 {

    private static final long serialVersionUID = 0;

    public Sachen136(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void updateState() {
        setPrgBank(0);
        setChrBank(output);
    }

    @Override
    public int readMemory(final int address) {
        return ((address & 0xE000) == 0x4000)
                ? (0xC0 | (readJV001(address) & 0x3F)) : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x4000) {
            writeJV001(address, value & 0x3F);
        } else {
            memory[address] = value;
        }
    }
}