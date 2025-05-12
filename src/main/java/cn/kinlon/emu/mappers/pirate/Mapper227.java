package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;

public class Mapper227 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper227(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = ((address & 0x0100) >> 3) | ((address & 0x007C) >> 2);
        final int OSL = ((address & 0x0080) >> 5) | ((address & 1) << 1)
                | ((address & 0x0200) >> 9);
        setNametableMirroring(getBit(address, 1));

        switch (OSL) {
            case 0:
                setPrgBank(2, bank);
                setPrgBank(3, bank & 0x38);
                break;
            case 1:
                setPrgBank(2, bank);
                setPrgBank(3, bank | 0x07);
                break;
            case 2:
                setPrgBank(2, bank & 0x3E);
                setPrgBank(3, bank & 0x38);
                break;
            case 3:
                setPrgBank(2, bank & 0x3E);
                setPrgBank(3, bank | 0x07);
                break;
            case 4:
            case 5:
                setPrgBank(2, bank);
                setPrgBank(3, bank);
                break;
            case 6:
            case 7:
                setPrgBanks(2, 2, bank & 0xFE);
                break;
        }
    }
}