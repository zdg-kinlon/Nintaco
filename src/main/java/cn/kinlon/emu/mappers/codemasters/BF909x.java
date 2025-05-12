package cn.kinlon.emu.mappers.codemasters;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.ONE_SCREEN_A;
import static cn.kinlon.emu.utils.BitUtil.getBit;

public class BF909x extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean bf9097Mode;

    public BF909x(final CartFile cartFile) {
        super(cartFile, 4, 1);
        if (cartFile.getSubmapperNumber() == 1) {
            bf9097Mode = true;
        }
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, -1);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address == 0x9000) {
            bf9097Mode = true;
        }

        if (address >= 0xC000 || !bf9097Mode) {
            setPrgBank(2, value);
        } else if (address < 0xC000) {
            setNametableMirroring(ONE_SCREEN_A + getBit(value, 4));
        }
    }
}
