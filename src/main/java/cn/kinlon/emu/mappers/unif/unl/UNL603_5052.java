package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class UNL603_5052 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] LUT = {0x00, 0x02, 0x02, 0x03};

    private int register;

    public UNL603_5052(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x4020 && address < 0x8000) {
            return register;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x4020 && address < 0x8000) {
            register = LUT[value & 3];
        } else {
            super.writeMemory(address, value);
        }
    }
}
