package cn.kinlon.emu.mappers.ntdec;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.pirate.Mapper058;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper174 extends Mapper058 {

    private static final long serialVersionUID = 0;

    public Mapper174(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void writeRegister(int address, int value) {
        chrBanks[0] = (address & 0x000E) << 12;
        setNametableMirroring(getBit(address, 0));
        int prgBank = address & 0x0070;
        if (getBitBool(address, 6)) {
            prgBanks[0] = prgBanks[1] = prgBank << 10;
        } else {
            prgBanks[0] = (prgBank & 0x0060) << 11;
            prgBanks[1] = prgBanks[0] | 0x4000;
        }
    }
}
