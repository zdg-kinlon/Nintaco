package nintaco.mappers.nintendo;

import nintaco.files.CartFile;

public class MMC1A extends MMC1 {

    private static final long serialVersionUID = 0;

    public MMC1A(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void writePrgBankReg(final int value) {
        super.writePrgBankReg(value & 0xEF);
    }
}
