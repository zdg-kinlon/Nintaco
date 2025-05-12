package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;

public class Mapper293 extends Mapper {

    private static final long serialVersionUID = 0;

    private int mode;
    private int innerBank;
    private int outerBank;

    public Mapper293(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        mode = innerBank = outerBank = 0;
        setChrBank(0);
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        switch (mode) {
            case 0:
                setPrgBank(2, innerBank);
                setPrgBank(3, 7);
                break;
            case 1:
                setPrgBank(2, innerBank & 6);
                setPrgBank(3, 7);
                break;
            case 2:
                setPrgBank(2, innerBank);
                setPrgBank(3, innerBank);
                break;
            case 3:
                set2PrgBanks(2, innerBank & 6);
                break;
        }
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, outerBank | value);
    }

    private void writeFirstBanking(final int value) {
        innerBank = value & 7;
        mode = (mode & 0xFD) | ((value & 0x08) >> 2);
        updateState();
    }

    private void writeSecondBanking(final int value) {
        outerBank = ((value & 0x01) << 5) | ((value & 0x30) >> 1);
        mode = (mode & 0xFE) | ((value & 0x40) >> 6);
        setNametableMirroring(getBit(value, 7));
        updateState();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address >> 13) {
            case 4:
                writeFirstBanking(value);
                writeSecondBanking(value);
                break;
            case 5:
                writeSecondBanking(value);
                break;
            case 6:
                writeFirstBanking(value);
                break;
        }
    }
}
