package cn.kinlon.emu.mappers.daouinfosys;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.ONE_SCREEN_A;
import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class DaouInfosys extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] chrLow = new int[8];
    private final int[] chrHigh = new int[8];

    public DaouInfosys(final CartFile cartFile) {
        super(cartFile, 4, 8, 0xC000, 0x8000);
    }

    @Override
    public void init() {
        setPrgBank(3, -1);
        setNametableMirroring(ONE_SCREEN_A);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0xC010) {
            final int bank = ((address & 8) >> 1) | (address & 3);
            if (getBitBool(address, 2)) {
                chrHigh[bank] = value << 8;
            } else {
                chrLow[bank] = value;
            }
            setChrBank(bank, chrHigh[bank] | chrLow[bank]);
        } else if (address == 0xC010) {
            setPrgBank(2, value);
        } else if (address == 0xC014) {
            setNametableMirroring(getBit(value, 0));
        }
    }
}
