package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;


public class Sachen8259A extends Sachen8259B {

    private static final long serialVersionUID = 0;

    public Sachen8259A(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateChrBanks() {
        for (int i = 3; i >= 0; i--) {
            setChrBank(i, (chrRegs[simpleMode ? 0 : i] << 1) | i);
        }
    }
}