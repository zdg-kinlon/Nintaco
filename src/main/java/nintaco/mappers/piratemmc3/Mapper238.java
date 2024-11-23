package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper238 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] securityLUT = {0x00, 0x02, 0x02, 0x03};

    private int securityValue;

    public Mapper238(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x4020 && address <= 0x7FFF) {
            return securityValue;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address < 0x8000) {
            securityValue = securityLUT[value & 0x03];
        }
        super.writeMemory(address, value);
    }
}