package nintaco.mappers.jy;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.MMC3;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper351 extends MMC3 {

    private static final long serialVersionUID = 0;

    private int chrBase;
    private int prgBase;
    private int mode;

    public Mapper351(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        chrBase = prgBase = mode = 0;
        updateState();
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void writeChrRomBase(final int value) {
        chrBase = (0xFC & value) << 1;
        updateState();
    }

    private void writePrgRomBase(final int value) {
        prgBase = 0xFC & value;
        updateState();
    }

    private void writeBankingMode(final int value) {
        mode = 0xF0 & value;
        updateState();
    }

    @Override
    protected void updatePrgBanks() {
        if (getBitBool(mode, 7)) {
            if (getBitBool(mode, 6)) {
                set2PrgBanks(4, prgBase >> 1);
                set2PrgBanks(6, prgBase >> 1);
            } else {
                set4PrgBanks(4, prgBase >> 1);
            }
        } else {
            super.updatePrgBanks();
        }
    }

    private void updateState() {
        if (getBitBool(mode, 7)) {
            setPrgBlock(0, -1);
            setChrBlock(chrBase, 0x1F);
        } else {
            setPrgBlock(prgBase >> 1, getBitBool(mode, 5) ? 0x0F : 0x1F);
            setChrBlock(chrBase, getBitBool(mode, 5) ? 0x7F : 0xFF);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address & 0xF003) {
            case 0x5000:
                writeChrRomBase(value);
                break;
            case 0x5001:
                writePrgRomBase(value);
                break;
            case 0x5002:
                writeBankingMode(value);
                break;
            default:
                super.writeMemory(address, value);
                break;
        }
    }
}