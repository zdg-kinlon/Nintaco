package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.konami.VRC2And4;



public class AX40G extends VRC2And4 {

    private static final long serialVersionUID = 0;

    public AX40G(final CartFile cartFile) {
        super(cartFile);
        prgHigh = 0x20;
        variant = VRC2b;
    }

    @Override
    protected void writeMirroringControl(final int value) {
    }

    @Override
    protected void updateChrBanks() {
        super.updateChrBanks();
        setNametables((chrHigh[0] >> 3) & 3, (chrHigh[0] >> 3) & 3,
                (chrHigh[1] >> 3) & 3, (chrHigh[1] >> 3) & 3);
    }
}