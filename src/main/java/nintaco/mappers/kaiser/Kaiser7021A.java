package nintaco.mappers.kaiser;

import nintaco.files.CartFile;
import nintaco.mappers.konami.VRC2And4;

public class Kaiser7021A extends VRC2And4 {

    private static final long serialVersionUID = 0;

    public Kaiser7021A(final CartFile cartFile) {
        super(cartFile);
        prgHigh = 0x20;
        variant = VRC2b;
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0xF000) == 0xB000) {
            setChrBank(address & 7, value);
        } else {
            super.writeRegister(address, value);
        }
    }

    @Override
    protected void updateChrBanks() {
    }
}
