package nintaco.mappers.codemasters;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper232 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper232(final CartFile cartFile) {
        super(cartFile, 4, 0);
        prgBanks[3] = 0xC000;
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0xC000) {
            writeBlock(value);
        } else {
            writePage(value);
        }
    }

    private void writeBlock(final int value) {
        int block = (value & 0x18) << 13;
        prgBanks[2] = (prgBanks[2] & 0xC000) | block;
        prgBanks[3] = 0xC000 | block;
    }

    private void writePage(final int value) {
        prgBanks[2] = (prgBanks[2] & 0x30000) | ((value & 0x03) << 14);
    }
}
