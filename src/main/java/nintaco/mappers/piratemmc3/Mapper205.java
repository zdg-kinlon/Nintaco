package nintaco.mappers.piratemmc3;

import nintaco.files.*;

public class Mapper205 extends BlockMMC3 {

    private static final long serialVersionUID = 0;

    private static final int[][] BLOCKS = {
            {0x00, 0x1F, 0x000, 0xFF},
            {0x10, 0x1F, 0x080, 0xFF},
            {0x20, 0x0F, 0x100, 0x7F},
            {0x30, 0x0F, 0x180, 0x7F},
    };

    public Mapper205(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateBlock(final int value) {
        final int[] BS = BLOCKS[value & 0x03];
        setBlock(BS[0], BS[1], BS[2], BS[3]);
    }
}
