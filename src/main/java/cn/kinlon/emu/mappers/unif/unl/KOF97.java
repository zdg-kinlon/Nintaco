package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class KOF97 extends MMC3 {

    private static final long serialVersionUID = 0;

    public KOF97(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeRegister(int address, int value) {
        if (address < 0xA000) {
            value = (value & 0xD8) | ((value & 0x20) >> 4) | ((value & 4) << 3)
                    | ((value & 2) >> 1) | ((value & 1) << 2);
            if (address == 0x9000) {
                address = 0x8001;
            }
        } else {
            value = (value & 0xD8) | ((value & 0x20) >> 4) | ((value & 4) << 3)
                    | ((value & 2) >> 1) | ((value & 1) << 2);
            if (address == 0xD000) {
                address = 0xC001;
            } else if (address == 0xF000) {
                address = 0xE001;
            }
        }
        super.writeRegister(address, value);
    }
}