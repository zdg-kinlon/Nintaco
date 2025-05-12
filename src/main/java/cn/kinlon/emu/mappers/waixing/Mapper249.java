package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper249 extends MMC3 {

    private boolean scramble;

    public Mapper249(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setPrgBank(final int bank, int value) {
        if (scramble) {
            if (value < 0x20) {
                value = (value & 0x01) | ((value >> 3) & 0x02) | ((value >> 1) & 0x04)
                        | ((value << 2) & 0x18);
            } else {
                value -= 0x20;
                value = (value & 0x03) | ((value >> 1) & 0x04) | ((value >> 4) & 0x08)
                        | ((value >> 2) & 0x10) | ((value << 3) & 0x20)
                        | ((value << 2) & 0xC0);
            }
        }
        super.setPrgBank(bank, value);
    }

    @Override
    protected void setChrBank(final int bank, int value) {
        if (scramble) {
            value = (value & 0x03) | ((value >> 1) & 0x04) | ((value >> 4) & 0x08)
                    | ((value >> 2) & 0x10) | ((value << 3) & 0x20)
                    | ((value << 2) & 0xC0);
        }
        super.setChrBank(bank, value);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address == 0x5000) {
            scramble = getBitBool(value, 1);
            updateBanks();
        }
        super.writeMemory(address, value);
    }
}
