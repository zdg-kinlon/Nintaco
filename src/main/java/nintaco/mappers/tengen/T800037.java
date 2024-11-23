package nintaco.mappers.tengen;

import nintaco.files.NesFile;

public class T800037 extends RAMBO1 {

    private static final long serialVersionUID = 0;

    public T800037(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected void writeMirroring(int value) {
    }

    @Override
    protected void updateChrBanks() {
        super.updateChrBanks();
        for (int i = 3; i >= 0; i--) {
            setNametable(i, R[ChrModes[chrMode][i]]);
        }
    }

    @Override
    public void setNametable(final int index, final int value) {
        nametableMappings[index] = 0x2000 | ((value & 0x80) << 3);
    }
}
