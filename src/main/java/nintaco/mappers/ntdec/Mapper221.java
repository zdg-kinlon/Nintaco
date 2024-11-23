package nintaco.mappers.ntdec;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper221 extends Mapper {

    private static final long serialVersionUID = 0;

    private int mode;
    private int prgReg;

    public Mapper221(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setChrBank(0);
        updateBanks();
    }

    private void updateBanks() {
        final int outerBank = (mode & 0xFC) >> 2;
        if (getBitBool(mode, 1)) {
            if (getBitBool(mode, 8)) {
                setPrgBank(2, outerBank | prgReg);
                setPrgBank(3, outerBank | 0x07);
            } else {
                setPrgBanks(2, 2, outerBank | (prgReg & 0x06));
            }
        } else {
            setPrgBank(2, outerBank | prgReg);
            setPrgBank(3, outerBank | prgReg);
        }

        setNametableMirroring(mode & 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xC000) {
            case 0x8000:
                mode = address;
                break;
            case 0xC000:
                prgReg = address & 0x07;
                break;
        }
        updateBanks();
    }
}