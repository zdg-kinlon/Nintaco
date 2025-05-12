package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.GxROM;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper147 extends GxROM {

    private static final long serialVersionUID = 0;

    public Mapper147(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0x4103) == 0x4102) {
            chrBanks[0] = (value & 0x78) << 10;
            setPrgBank((getBit(value, 7) << 1) | getBit(value, 2));
        } else {
            memory[address] = value;
        }
    }
}
