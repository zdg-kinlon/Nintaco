package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.konami.VRC2And4;



public class T230 extends VRC2And4 {

    private static final long serialVersionUID = 0;

    public T230(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void detectVariant(final CartFile cartFile) {
        useHeuristics = true;
        variant = VRC4e;
    }

    @Override
    protected void writePrgSelect0(final int value) {
    }

    @Override
    protected void writePrgSelect1(final int value) {
        prgSelect0 = (value & 0x1F) << 1;
        prgSelect1 = ((value & 0x1F) << 1) | 1;
        updateBanks();
    }
}