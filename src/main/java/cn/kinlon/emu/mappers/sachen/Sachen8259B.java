package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;


public class Sachen8259B extends Sachen8259 {

    private static final long serialVersionUID = 0;

    public Sachen8259B(final CartFile cartFile) {
        super(cartFile);
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
                for (int i = 0; i < 4; i++) {
                    writeChrTopBits(i, value);
                }
                break;
            case 5:
                setPrgBank(value);
                break;
            case 7:
                writeModeAndMirroringSelect(value);
                break;
        }
    }

    protected void writeChrTopBits(int bank, int value) {
        chrRegs[bank] = (chrRegs[bank] & 0xC7) | ((value & 0x07) << 3);
        updateChrBanks();
    }

    @Override
    protected void updateChrBanks() {
        for (int i = 3; i >= 0; i--) {
            chrBanks[i] = chrRegs[simpleMode ? 0 : i] << 11;
        }
    }
}
