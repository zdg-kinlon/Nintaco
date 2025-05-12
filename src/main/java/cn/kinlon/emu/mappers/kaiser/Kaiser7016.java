package cn.kinlon.emu.mappers.kaiser;

// TODO ENHANCE WITH FDS AUDIO

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Kaiser7016 extends Mapper {

    private static final long serialVersionUID = 0;

    public Kaiser7016(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBank(3, 0x08);
        set4PrgBanks(4, 0x0C);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xD943) {
            case 0xD943:
                if ((address & 0x30) == 0x30) {
                    setPrgBank(3, 0x0B);
                } else {
                    setPrgBank(3, (address >> 2) & 0x0F);
                }
                break;
            case 0xD903:
                if ((address & 0x30) == 0x30) {
                    setPrgBank(3, 0x08 | ((address >> 2) & 0x03));
                } else {
                    setPrgBank(3, 0x0B);
                }
                break;
        }
    }
}