package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class BMC60311C extends Mapper {

    private static final long serialVersionUID = 0;

    private int innerBank;
    private int outerBank;
    private int mode;

    public BMC60311C(final CartFile cartFile) {
        super(cartFile, 4, 1, 0x6000, 0x8000);
    }

    @Override
    public void init() {
        innerBank = outerBank = mode = 0;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        final int value = outerBank | (getBitBool(mode, 2) ? 0x00 : innerBank);
        switch (mode & 3) {
            case 0:
                setPrgBank(2, value);
                setPrgBank(3, value);
                break;
            case 1:
                set2PrgBanks(2, value & 0xFE);
                break;
            case 2:
                setPrgBank(2, value);
                setPrgBank(3, outerBank | 7);
                break;
        }
        setNametableMirroring(getBit(mode, 3));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0x8000) {
            innerBank = value & 7;
        } else if ((address & 1) == 0) {
            mode = value;
        } else {
            outerBank = value;
        }
        updateState();
    }
}