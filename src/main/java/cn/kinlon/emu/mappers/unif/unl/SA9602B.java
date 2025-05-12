package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class SA9602B extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] EXPREGS = new int[2];

    public SA9602B(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected int getChrRamSize(final CartFile cartFile) {
        return 0x8000;
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, (EXPREGS[1] << 6) | value);
        if (prgMode) {
            super.setPrgBank(4, 62);
        } else {
            super.setPrgBank(6, 62);
        }
        super.setPrgBank(7, 63);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if (address < 0xC000) {
            switch (address & 0xE001) {
                case 0x8000:
                    EXPREGS[0] = value;
                    break;
                case 0x8001:
                    if ((EXPREGS[0] & 7) < 6) {
                        EXPREGS[1] = value >> 6;
                        updatePrgBanks();
                    }
                    break;
            }
        }
        super.writeRegister(address, value);
    }
}
