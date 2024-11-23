package nintaco.mappers.txc;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper189 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int prgReg;

    public Mapper189(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void setPrgBank(final int bank, final int value) {
        final int B = (prgReg & 7) << 2;
        super.setPrgBank(4, B);
        super.setPrgBank(5, B | 1);
        super.setPrgBank(6, B | 2);
        super.setPrgBank(7, B | 3);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x8000) {
            writeRegister(address, value);
        } else if (address >= 0x4120) {
            prgReg = value | (value >> 4);
            updatePrgBanks();
        }
    }
}