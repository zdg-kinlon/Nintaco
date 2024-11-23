package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.konami.*;

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