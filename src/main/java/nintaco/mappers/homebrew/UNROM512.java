package nintaco.mappers.homebrew;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class UNROM512 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean mapperControlledMirroring;

    public UNROM512(final CartFile cartFile) {
        super(cartFile, 4, 1);
        setPrgBank(3, -1);
    }

    @Override
    protected int getChrRamSize(final CartFile cartFile) {
        return 0x8000;
    }

    @Override
    public void init() {
        mapperControlledMirroring = nametableMirroring >= 2;
    }

    @Override
    protected void writeRegister(int address, int value) {
//    $8000:  [MCCP PPPP]
//      M = One screen Mirroring select
//      C = CHR RAM bank
//      P = PRG ROM bank
        setPrgBank(2, value & 0x1F);
        setChrBank((value >> 5) & 3);
        if (mapperControlledMirroring) {
            setNametableMirroring(getBit(value, 7) + 2);
        }
    }
}
