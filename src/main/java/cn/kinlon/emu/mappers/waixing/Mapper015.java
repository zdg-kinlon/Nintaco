package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper015 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper015(final CartFile cartFile) {
        super(cartFile, 8, 0);
    }

    @Override
    public void init() {
        setPrgBanks(4, 4, -4);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        setNametableMirroring(getBit(value, 6));

        final int high = (value & 0x7F) << 1;
        final int low = value >> 7;
        switch (address & 0x8FFF) {
            case 0x8000:
                for (int i = 3; i >= 0; i--) {
                    setPrgBank(4 | i, (high + i) ^ low);
                }
                break;
            case 0x8001:
            case 0x8003:
                for (int i = 3; i >= 0; i--) {
                    int b = value & 0x7F;
                    if (i >= 2 && !getBitBool(address, 1)) {
                        b = 0x7F;
                    }
                    setPrgBank(4 | i, (i & 1) + ((b << 1) ^ low));
                }
                break;
            case 0x8002: {
                final int bank = high | low;
                for (int i = 3; i >= 0; i--) {
                    setPrgBank(4 | i, bank);
                }
                break;
            }
        }
    }
}