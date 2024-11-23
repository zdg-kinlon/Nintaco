package nintaco.mappers.jy;

// TODO SHAKY MENU BARS
// TODO BAD NAMETABLE BANKING?

import nintaco.files.CartFile;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper282 extends JY {

    private static final long serialVersionUID = 0;

    protected int prgBlockOffset;
    protected int chrBlockOffset;

    public Mapper282(final CartFile cartFile) {
        super(cartFile, 282);
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
            chrBlockOffset = (value & 0x01);
            if (getBitBool(value, 4)) {
                chrBlockOffset |= (value & 0x08) >> 2;
            }
            chrBlockOffset <<= 8;
            updatePrgBanks();
            updateChrBanks();
        } else {
            super.writeMemory(address, value);
        }
    }
}
