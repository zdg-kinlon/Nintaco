package nintaco.mappers.subor;

import nintaco.files.*;
import nintaco.mappers.*;

public class Mapper166 extends Mapper {

    private static final long serialVersionUID = 0;

    protected int outerBank;
    protected int mode;
    protected int innerBank1;
    protected int innerBank2;

    public Mapper166(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xE000) {
            case 0x8000:
                writeOuterBank(value);
                break;
            case 0xA000:
                writeMode(value);
                break;
            case 0xC000:
                writeInnerBank1(value);
                break;
            case 0xE000:
                writeInnerBank2(value);
                break;
        }
    }

    protected void writeOuterBank(final int value) {
        outerBank = value;
        updateBanks();
    }

    protected void writeMode(final int value) {
        mode = value;
        updateBanks();
    }

    protected void writeInnerBank1(final int value) {
        innerBank1 = value;
        updateBanks();
    }

    protected void writeInnerBank2(final int value) {
        innerBank2 = value;
        updateBanks();
    }

    protected void updateBanks() {
        int outer512 = ((outerBank ^ mode) & 0x10) << 1;
        int inner16 = (innerBank1 ^ innerBank2) & 0x1F;
        int bank = outer512 | inner16;
        switch (mode & 0x0F) {
            case 0x00:
                updateUNROM(bank);
                break;
            case 0x04:
                updateInvertedUNROM(bank);
                break;
            case 0x08:
            case 0x0C:
                updateNROM(bank);
                break;
        }
    }

    protected void updateUNROM(final int bank) {
        setPrgBank(2, bank);
        setPrgBank(3, 0x07);
    }

    protected void updateInvertedUNROM(final int bank) {
        setPrgBank(2, 0x1F);
        setPrgBank(3, bank);
    }

    protected void updateNROM(final int bank) {
        setPrgBank(2, bank & 0xFE);
        setPrgBank(3, bank | 0x01);
    }
}
