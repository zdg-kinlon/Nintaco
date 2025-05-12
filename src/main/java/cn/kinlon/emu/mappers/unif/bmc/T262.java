package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class T262 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean lock;
    private boolean mode;
    private int base;

    public T262(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    public void resetting() {
        mode = lock = false;
        base = 0;
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (!lock) {
            lock = getBitBool(address, 13);
            base = ((address & 0x0060) >> 2) | ((address & 0x0100) >> 3);
            mode = getBitBool(address, 7);
            setNametableMirroring(getBit(address, 1));
        }
        final int bank = value & 7;
        setPrgBank(2, base | bank);
        setPrgBank(3, base | (mode ? bank : 7));
    }
}
