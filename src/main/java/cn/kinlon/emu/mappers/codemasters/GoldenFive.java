package cn.kinlon.emu.mappers.codemasters;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class GoldenFive extends Mapper {

    private static final long serialVersionUID = 0;

    public GoldenFive(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, 0x0F);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0xC000) {
            prgBanks[2] = (prgBanks[2] & 0x1C0000) | ((value & 0x0F) << 14);
        } else if ((address & 0xE000) == 0x8000) {
            if (getBitBool(value, 3)) {
                prgBanks[2] = (((value << 4) & 0x70) << 14) | (prgBanks[2] & 0x3C000);
                setPrgBank(3, ((value << 4) & 0x70) | 0x0F);
            }
        }
    }
}