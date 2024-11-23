package nintaco.mappers.homebrew;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

// TODO PARTIAL IMPLEMENTATION, NO TEST ROM

public class Glider extends Mapper {

    private static final long serialVersionUID = 0;

    public Glider(final CartFile cartFile) {
        super(cartFile, 4, 1);
        xram = new int[0x8000];
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank(value & 0x07);
        prgBanks[2] = (value & 0x1C) << 12;
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return xram[chrBanks[0] | address];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            xram[chrBanks[0] | address] = value;
        } else {
            vram[address] = value;
        }
    }
}
