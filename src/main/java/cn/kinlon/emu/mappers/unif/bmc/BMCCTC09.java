package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCCTC09 extends Mapper {

    private static final long serialVersionUID = 0;

    public BMCCTC09(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        writeChrRom(0);
        writePrgRomAndMirroring(0);
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeChrRom(final int value) {
        setChrBank(value & 0x0F);
    }

    private void writePrgRomAndMirroring(final int value) {
        int v = (value & 7) << 1;
        if (getBitBool(value, 4)) {
            v |= (value >> 3) & 1;
            setPrgBank(2, v);
            setPrgBank(3, v);
        } else {
            set2PrgBanks(2, v);
        }
        setNametableMirroring(getBit(value, 5));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0xC000) {
            writeChrRom(value);
        } else {
            writePrgRomAndMirroring(value);
        }
    }
}
