package nintaco.mappers.homebrew;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.VERTICAL;

public class MagicKidGooGoo extends Mapper {

    private static final long serialVersionUID = 0;

    public MagicKidGooGoo(final CartFile cartFile) {
        super(cartFile, 4, 4);
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, 0);
        setChrBanks(0, 4, 0);
        setNametableMirroring(VERTICAL);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if ((address & 0xE000) == 0x8000) {
            setPrgBank(2, value & 7);
        } else if ((address & 0xE000) == 0xC000) {
            setPrgBank(2, 0x08 | (value & 7));
        } else if ((address & 0xA000) == 0xA000) {
            setChrBank(address & 3, value);
        }
    }
}