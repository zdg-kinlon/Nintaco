package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.konami.VRC2And4;

// TODO THIS APPEARS TO BE WORKING WITHOUT ADDITIONAL REGS!

public class Mapper520 extends VRC2And4 {

    private static final long serialVersionUID = 0;

    public Mapper520(final CartFile cartFile) {
        super(cartFile);
        prgHigh = 0x20;
        variant = VRC4e;
    }
}
