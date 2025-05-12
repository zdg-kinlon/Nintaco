package cn.kinlon.emu.mappers.ae;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.pirate.Mapper062;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper228 extends Mapper062 {

    private static final long serialVersionUID = 0;

    public Mapper228(final CartFile cartFile) {
        super(cartFile);
    }


    @Override
    public void init() {
        writeRegister(0x8000, 0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        int chip = (address >> 11) & 0x03;
        if (chip == 3) {
            chip = 2;
        }
        writePrgBanks(((address >> 6) & 0x1F) | (chip << 5),
                getBitBool(address, 5));
        setChrBank(0, ((address & 0x000F) << 2) | (value & 0x03));
        setNametableMirroring(getBit(address, 13));
    }
}
