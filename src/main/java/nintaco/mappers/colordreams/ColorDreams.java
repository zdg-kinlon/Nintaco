package nintaco.mappers.colordreams;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.GxROM;

public class ColorDreams extends GxROM {

    private static final long serialVersionUID = 0;

    public ColorDreams(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(value & 0x03);
        chrBanks[0] = (value & 0xF0) << 9;
    }
}
