package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class Mapper254 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int xorMask;
    private boolean applyXorMask = true;

    public Mapper254(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readMemory(final int address) {
        final int value = super.readMemory(address);
        if (applyXorMask && (address & 0xE000) == 0x6000) {
            return value ^ xorMask;
        } else {
            return value;
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {
        switch (address) {
            case 0x8000:
                applyXorMask = false;
                break;
            case 0xA001:
                xorMask = value;
                break;
        }
        super.writeRegister(address, value);
    }
}
