package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.piratemmc3.BlockMMC3;

public class Mapper037 extends BlockMMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] PRG_MASKS
            = {0x07, 0x07, 0x07, 0x07, 0x0F, 0x0F, 0x0F, 0x07};
    private static final int[] PRG_OFFSETS
            = {0x00, 0x00, 0x00, 0x08, 0x10, 0x10, 0x10, 0x18};

    public Mapper037(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateBlock(int value) {
        value &= 7;
        setBlock(PRG_OFFSETS[value], PRG_MASKS[value], (value & 0x04) << 5, 0x7F);
    }
}
