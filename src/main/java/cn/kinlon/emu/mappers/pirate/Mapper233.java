package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;

public class Mapper233 extends Mapper226 {

    private static final long serialVersionUID = 0;

    private int reset;

    public Mapper233(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void resetting() {
        super.resetting();
        reset ^= 0x01;
        updatePrg();
    }

    @Override
    protected int getPrgPage() {
        return (registers[0] & 0x1F) | (reset << 5) | ((registers[1] & 0x01) << 6);
    }
}