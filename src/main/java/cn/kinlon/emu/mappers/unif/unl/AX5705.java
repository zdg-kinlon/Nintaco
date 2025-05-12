package cn.kinlon.emu.mappers.unif.unl;

import java.util.*;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class AX5705 extends Mapper {

    private static final long serialVersionUID = 0;

    public AX5705(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBank(6, -2);
        setPrgBank(7, -1);
        Arrays.fill(chrBanks, 0);
    }

    private void setChrBank(final int bank, final int value, final boolean high) {
        if (high) {
            chrBanks[bank] = (chrBanks[bank] & 0x03C00) | (((((value & 0x04) >> 1)
                    | ((value & 0x02) << 1) | (value & 0x09)) << 4) << 10);
        } else {
            chrBanks[bank] = (chrBanks[bank] & 0x3C000) | ((value & 0x0F) << 10);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0xA008) {
            final boolean high = getBitBool(address, 0);
            switch (address & 0xF00E) {
                case 0xA008:
                    setChrBank(0, value, high);
                    break;
                case 0xA00A:
                    setChrBank(1, value, high);
                    break;
                case 0xC000:
                    setChrBank(2, value, high);
                    break;
                case 0xC002:
                    setChrBank(3, value, high);
                    break;
                case 0xC008:
                    setChrBank(4, value, high);
                    break;
                case 0xC00A:
                    setChrBank(5, value, high);
                    break;
                case 0xE000:
                    setChrBank(6, value, high);
                    break;
                case 0xE002:
                    setChrBank(7, value, high);
                    break;
            }
        } else {
            switch (address & 0xF00F) {
                case 0x8000:
                    setPrgBank(4, ((value & 0x02) << 2) | ((value & 0x08) >> 2)
                            | (value & 0x05));
                    break;
                case 0x8008:
                    setNametableMirroring(value & 1);
                    break;
                case 0xA000:
                    setPrgBank(5, ((value & 0x02) << 2) | ((value & 0x08) >> 2)
                            | (value & 0x05));
                    break;
            }
        }
    }
}