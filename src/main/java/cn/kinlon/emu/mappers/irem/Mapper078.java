package cn.kinlon.emu.mappers.irem;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;

public class Mapper078 extends Mapper {

    private static final long serialVersionUID = 0;

    private final boolean holyDiver;

    public Mapper078(final CartFile cartFile) {
        super(cartFile, 4, 1);
        holyDiver = cartFile.getSubmapperNumber() == 3;
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(2, value & 0x07);
        chrBanks[0] = (value & 0xF0) << 9;
        if (holyDiver) {
            setNametableMirroring(getBit(value, 3) ^ 1);
        } else {
            setNametableMirroring(getBit(value, 3) + 2);
        }
    }
}