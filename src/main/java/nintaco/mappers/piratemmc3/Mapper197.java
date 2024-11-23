package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper197 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper197(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateChrBanks() {
        if (chrMode) {
            setChrBanks(0, 4, R[2] << 1);
            setChrBanks(4, 2, R[0] << 1);
            setChrBanks(6, 2, R[0] << 1);
        } else {
            setChrBanks(0, 4, R[0] << 1);
            setChrBanks(4, 2, R[2] << 1);
            setChrBanks(6, 2, R[3] << 1);
        }
    }
}
