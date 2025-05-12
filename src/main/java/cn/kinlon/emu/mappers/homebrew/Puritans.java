package cn.kinlon.emu.mappers.homebrew;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Puritans extends Mapper {

    private static final long serialVersionUID = 0;

    public Puritans(final CartFile cartFile) {
        super(cartFile, 16, 0, 0x5000, 0x8000);
    }

    @Override
    public void init() {
        writeRegister(0x5FFF, 0xFF);
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (address < 0x6000) {
            setPrgBank(8 | (address & 7), value);
        }
    }
}
