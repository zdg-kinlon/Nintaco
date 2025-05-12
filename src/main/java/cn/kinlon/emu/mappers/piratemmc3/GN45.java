package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.*;

public class GN45 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean locked;

    public GN45(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        locked = false;
        writeOuterBankRegister(0);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeOuterBankRegister(int address) {
        if (!locked) {
            locked = getBitBool(address, 7);
            address &= 0x0070;
            setPrgBlock(address, 0x0F);
            setChrBlock(address << 3, 0x7F);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xFF00) == 0x6800) {
            writeOuterBankRegister(address);
        }
        super.writeMemory(address, value);
    }
}
