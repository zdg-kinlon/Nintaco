package nintaco.mappers.jy;

import nintaco.files.CartFile;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper358 extends JY {

    private static final long serialVersionUID = 0;

    protected int prgBlockMask = -1;
    protected int prgBlockOffset;
    protected int chrBlockMask = -1;
    protected int chrBlockOffset;

    public Mapper358(final CartFile cartFile) {
        super(cartFile, 358);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, prgBlockOffset | (0x3F & value));
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, chrBlockOffset | (chrBlockMask & value));
    }

    private void writeOuterBankRegister(final int value) {
        prgBlockOffset = (value & 6) << 5;
        if (getBitBool(value, 5)) {
            chrBlockOffset = 0x200;
            chrBlockMask = 0x1FF;
        } else {
            chrBlockOffset = (value & 1) << 8;
            chrBlockMask = 0x0FF;
        }
        updateChrBanks();
        updatePrgBanks();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((0xF803 & address) == 0xD003) {
            writeOuterBankRegister(value);
        }
        super.writeMemory(address, value);
    }
}