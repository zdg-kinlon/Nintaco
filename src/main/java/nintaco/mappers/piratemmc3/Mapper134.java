package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper134 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int reg;

    public Mapper134(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, (value & 0xFF) | ((reg & 0x20) << 3));
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, (value & 0x1F) | ((reg & 0x02) << 4));
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address == 0x6001) {
            reg = value;
            updateBanks();
        } else if (address >= minRegisterAddress) {
            writeRegister(address, value);
        }
    }
}