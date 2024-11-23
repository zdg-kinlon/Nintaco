package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.piratemmc3.BlockMMC3;

public class Mapper047 extends BlockMMC3 {

    private static final long serialVersionUID = 0;

    public Mapper047(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateBlock(int value) {
        value &= 1;
        setBlock(value << 4, 0x0F, value << 7, 0x7F);
    }
}
