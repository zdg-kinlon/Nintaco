package nintaco.mappers.jy;

import nintaco.files.CartFile;

public class Mapper281 extends JY {

    private static final long serialVersionUID = 0;

    protected int prgBlockOffset;
    protected int chrBlockOffset;

    public Mapper281(final CartFile cartFile) {
        super(cartFile, 281);
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
            value &= 1;
            prgBlockOffset = value << 5;
            chrBlockOffset = value << 8;
            updatePrgBanks();
            updateChrBanks();
        }
        super.writeMemory(address, value);
    }
}
