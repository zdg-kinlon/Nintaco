package nintaco.mappers.waixing;

import nintaco.files.*;
import nintaco.mappers.*;

public class Mapper176 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean SBW;

    public Mapper176(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x5001, 0x8000);
    }

    @Override
    public void init() {
        super.init();
        SBW = false;
        setPrgBank(4, 0);
        setPrgBank(5, 1);
        setPrgBank(6, 62);
        setPrgBank(7, 63);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(int address, int value) {
        switch (address) {
            case 0x5001:
                if (SBW) {
                    writePrgBanks(value);
                }
                break;
            case 0x5010:
                if (value == 0x24) {
                    SBW = true;
                }
                break;
            case 0x5011:
                if (SBW) {
                    writePrgBanks(value >> 1);
                }
                break;
            case 0x5FF1:
                writePrgBanks(value >> 1);
                break;
            case 0x5FF2:
                setChrBank(0, value);
                break;
            case 0xA000:
                setNametableMirroring(value & 3);
                break;
        }
    }

    private void writePrgBanks(int value) {
        setBanks(prgBanks, 4, value << 15, 4, 0x2000);
    }
}
