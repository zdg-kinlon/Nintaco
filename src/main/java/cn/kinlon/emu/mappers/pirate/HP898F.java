package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;

public class HP898F extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[2];

    public HP898F(final CartFile cartFile) {
        super(cartFile, 4, 1, 0x6000, 0x8000);
    }

    @Override
    public void init() {
        regs[0] = regs[1] = 0;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        final int prgReg = (regs[1] >> 3) & 7;
        final int prgMask = (regs[1] >> 4) & 4;
        setChrBank((((regs[0] >> 4) & 0x07) & ~(((regs[0] & 0x01) << 2)
                | (regs[0] & 0x02))));
        setPrgBank(2, prgReg & (~prgMask));
        setPrgBank(3, prgReg | prgMask);
        setNametableMirroring(getBit(regs[1], 7) ^ 1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0x6000) == 0x6000) {
            regs[(address & 0x04) >> 2] = value;
            updateState();
        }
    }
}
