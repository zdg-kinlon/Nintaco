package nintaco.mappers.sachen;

import nintaco.files.*;

import static nintaco.util.BitUtil.*;

public class Sachen8259D extends Sachen8259 {

    private static final long serialVersionUID = 0;

    public Sachen8259D(final CartFile cartFile) {
        super(cartFile, 2, 8);
        setChrBank(4, -4);
        setChrBank(5, -3);
        setChrBank(6, -2);
        setChrBank(7, -1);
    }

    @Override
    public void init() {
    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address & 0xC101) {
            case 0x4100:
                writeRegisterSelect(value);
                break;
            case 0x4000:
            case 0x4001:
            case 0x4101:
                writeRegisterData(value);
                break;
            default:
                memory[address] = value;
                break;
        }
    }

    @Override
    protected void writeRegisterData(int value) {
        value &= 7;
        switch (register) {
            case 0:
                writeChrSelect(0, value);
                break;
            case 1:
                writeChrSelect(1, value);
                break;
            case 2:
                writeChrSelect(2, value);
                break;
            case 3:
                writeChrSelect(3, value);
                break;
            case 4:
                writeChrTopBit(1, value, 0);
                writeChrTopBit(2, value, 1);
                writeChrTopBit(3, value, 2);
                break;
            case 5:
                setPrgBank(value);
                break;
            case 6:
                chrRegs[3] = (chrRegs[3] & 0xF7) | ((value & 1) << 3);
                updateChrBanks();
                break;
            case 7:
                writeModeAndMirroringSelect(value);
                break;
        }
    }

    private void writeChrTopBit(final int bank, final int value, final int bit) {
        chrRegs[bank] = (chrRegs[bank] & 0xEF) | (getBit(value, bit) << 4);
        updateChrBanks();
    }

    @Override
    protected void updateChrBanks() {
        for (int i = 3; i >= 0; i--) {
            setChrBank(i, chrRegs[i]);
        }
    }
}
