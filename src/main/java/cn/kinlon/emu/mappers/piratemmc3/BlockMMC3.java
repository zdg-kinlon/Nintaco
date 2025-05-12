package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

public abstract class BlockMMC3 extends MMC3 {

    private static final long serialVersionUID = 0;

    protected boolean sramRegister = true;

    public BlockMMC3(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        super.init();
        updateBlock(0);
    }

    protected abstract void updateBlock(final int value);

    @Override
    public void writeMemory(final int address, final int value) {
        if (sramRegister && prgRamWritesEnabled && (address & 0xE000) == 0x6000) {
            updateBlock(value);
        } else {
            super.writeMemory(address, value);
        }
    }
}

