package nintaco.mappers.nintendo;

import nintaco.files.CartFile;

public class TxSROM extends MMC3 {

    private static final long serialVersionUID = 0;

    public TxSROM(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void writeMirroring(final int value) {
    }

    @Override
    protected void updateChrBanks() {
        super.updateChrBanks();
        if (chrMode) {
            setNametable(0, R[2]);
            setNametable(1, R[3]);
            setNametable(2, R[4]);
            setNametable(3, R[5]);
        } else {
            setNametable(0, R[0]);
            setNametable(1, R[0]);
            setNametable(2, R[1]);
            setNametable(3, R[1]);
        }
    }

    @Override
    public void setNametable(final int index, final int value) {
        nametableMappings[index] = 0x2000 | ((value & 0x80) << 3);
    }
}
