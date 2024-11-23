package nintaco.mappers.bitcorp;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.GxROM;

public class CrimeBusters extends GxROM {

    private static final long serialVersionUID = 0;

    public CrimeBusters(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0x7000) == 0x7000) {
            prgBanks[1] = (value & 0x03) << 15;
            chrBanks[0] = (value & 0xFC) << 11;
        } else {
            memory[address] = value;
        }
    }
}