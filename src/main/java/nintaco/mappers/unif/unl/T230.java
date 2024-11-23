package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.konami.*;

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