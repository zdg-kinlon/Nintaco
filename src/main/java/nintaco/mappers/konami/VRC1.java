package nintaco.mappers.konami;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.FOUR_SCREEN;
import static nintaco.util.BitUtil.getBit;

public class VRC1 extends Mapper {

    private static final long serialVersionUID = 0;

    public VRC1(final CartFile cartFile) {
        super(cartFile, 8, 2);
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
                writePrgBank(4, value);
                break;
            case 0x9000:
                writeMirroring(value);
                break;
            case 0xA000:
                writePrgBank(5, value);
                break;
            case 0xC000:
                writePrgBank(6, value);
                break;
            case 0xE000:
                writeChrBank(0, value);
                break;
            case 0xF000:
                writeChrBank(1, value);
                break;
        }
    }

    private void writeMirroring(final int value) {
        if (nametableMirroring != FOUR_SCREEN) {
            setNametableMirroring(getBit(value, 0));
        }
        chrBanks[0] = (chrBanks[0] & 0x0F000) | ((value & 0x02) << 15);
        chrBanks[1] = (chrBanks[1] & 0x0F000) | ((value & 0x04) << 14);
    }

    private void writePrgBank(final int bank, final int value) {
        setPrgBank(bank, value & 0x0F);
    }

    private void writeChrBank(final int bank, final int value) {
        chrBanks[bank] = (chrBanks[bank] & 0x10000) | ((value & 0x0F) << 12);
    }
}
