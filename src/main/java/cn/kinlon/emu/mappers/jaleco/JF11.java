package cn.kinlon.emu.mappers.jaleco;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

public class JF11 extends Mapper {

    private static final long serialVersionUID = 0;

    public JF11(NesFile nesFile) {
        super(nesFile, 2, 1, 0x6000, 0x8000);
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (address < 0x8000) {
            setPrgBank((value >> 4) & 3);
            setChrBank(value & 0x0F);
        }
    }
}
