package nintaco.mappers.jy;

// TODO WIP

import nintaco.files.CartFile;

public class Mapper363 extends JY {

    private static final long serialVersionUID = 0;

    protected int prgBlockOffset;
    protected int chrBlockOffset;

    public Mapper363(final CartFile cartFile) {
        super(cartFile, 211); // Must be 211
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, prgBlockOffset | (value & 0x1F));
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, chrBlockOffset | value);
    }

    @Override
    public void writeMemory(final int address, int value) {
        if ((address & 0xF003) == 0xD003) {
            prgBlockOffset = (value & 0x06) << 4;
            chrBlockOffset = (((value & 0x04) >> 1) | (value & 0x01)) << 8;
            updatePrgBanks();
            updateChrBanks();
        }
        super.writeMemory(address, value);
    }
}
