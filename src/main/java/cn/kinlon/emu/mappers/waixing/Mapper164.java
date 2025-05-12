package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class Mapper164 extends Mapper {

    private static final long serialVersionUID = 0;

    private int prgBank;

    public Mapper164(final CartFile cartFile) {
        super(cartFile, 2, 1, 0x5000, 0x8000);
    }

    @Override
    public void init() {
        prgBank = 0x0F;
        setPrgBank(prgBank);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            return 0xFF;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0x7300) {
            case 0x5000:
                prgBank = (prgBank & 0xF0) | (value & 0x0F);
                setPrgBank(prgBank);
                break;
            case 0x5100:
                prgBank = (prgBank & 0x0F) | ((value & 0x0F) << 4);
                setPrgBank(prgBank);
                break;
        }
    }
}