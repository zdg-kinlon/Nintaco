package nintaco.mappers.nintendo;

import nintaco.files.CartFile;

public class Mapper185 extends CNROM {

    private static final long serialVersionUID = 0;

    protected boolean chrEnabled;
    protected boolean seicross;

    public Mapper185(final CartFile cartFile) {
        super(cartFile);
        seicross = cartFile.getFileCRC() == 0x0F05FF0A
                || cartFile.getFileName().contains("seic");
    }

    @Override
    public void init() {
        super.init();
        for (int i = 0x1FFF; i >= 0; i--) {
            vram[i] = 0xFF;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank(0, value & 3);
        final int C = value & 0x33;
        chrEnabled = seicross ? (C != 0x21) : ((C & 0x03) != 0 && C != 0x13);
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return chrEnabled ? super.readVRAM(address) : 0xFF;
        } else {
            return vram[address];
        }
    }
}