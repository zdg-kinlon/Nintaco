package nintaco.mappers.bitcorp;

// TODO DIPS

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper360 extends Mapper {

    private static final long serialVersionUID = 0;

    private int game;

    public Mapper360(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        if (game < 2) {
            set2PrgBanks(2, game & 0x1E);
        } else {
            setPrgBank(2, game);
            setPrgBank(3, game);
        }
        setChrBank(game);
        setNametableMirroring(game >> 4);
    }

    @Override
    public void resetting() {
        game = (game < 2) ? 2 : ((game + 1) & 0x1F);
        init();
    }
}
