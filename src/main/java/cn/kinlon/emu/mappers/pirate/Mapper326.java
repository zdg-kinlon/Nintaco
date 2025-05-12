package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper326 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper326(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        set4PrgBanks(4, -4);
        set8ChrBanks(0, 0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        switch (address & 0xE010) {
            case 0x8000:
                setPrgBank(4, value);
                break;
            case 0xA000:
                setPrgBank(5, value);
                break;
            case 0xC000:
                setPrgBank(6, value);
                break;
        }

        switch (address & 0x8018) {
            case 0x8010:
                setChrBank(address & 7, value);
                break;
            case 0x8018:
                setNametable(address & 3, value & 1);
                break;
        }
    }
}