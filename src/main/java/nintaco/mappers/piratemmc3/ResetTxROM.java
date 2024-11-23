package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class ResetTxROM extends MMC3 {

    private static final long serialVersionUID = 0;

    private int resets;

    public ResetTxROM(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void resetting() {
        resets = (resets + 1) & 3;
        updateBanks();
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, (resets << 7) | (value & 0x7F));
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, (resets << 4) | (value & 0x0F));
    }
}