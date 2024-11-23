package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper060 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean cartMode;
    private final boolean bmcVT5201;
    private int game;

    public Mapper060(final CartFile cartFile) {
        super(cartFile, 4, 1);
        bmcVT5201 = !(prgRomLength == (64 << 10) && chrRomLength == (32 << 10));
    }

    @Override
    public void init() {
        if (bmcVT5201) {
            setPrgBank(2, 0);
            setPrgBank(3, 1);
            setChrBank(0, 0);
            writeRegister(0x8000, 0x00);
        } else {
            setPrgBank(2, game);
            setPrgBank(3, game);
            setChrBank(0, game);
        }
    }

    @Override
    public void resetting() {
        game = (game + 1) & 3;
        init();
    }

    @Override
    public int readMemory(final int address) {
        if (bmcVT5201 && cartMode && address >= 0x8000) {
            return game;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (bmcVT5201) {
            cartMode = getBitBool(address, 8);
            setNametableMirroring((address & 8) >> 3);
            setPrgBank(2, (address >> 4) & ~(~address >> 7 & 0x1));
            setPrgBank(3, (address >> 4) | (~address >> 7 & 0x1));
            setChrBank(address);
        }
    }
}