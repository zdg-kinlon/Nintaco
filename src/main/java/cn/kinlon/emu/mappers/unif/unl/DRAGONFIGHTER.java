package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class DRAGONFIGHTER extends MMC3 {

    private int reg0;
    private int reg1;
    private int reg2;

    public DRAGONFIGHTER(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        if (bank == 4) {
            super.setPrgBank(4, reg0 & 0x1F);
        } else {
            super.setPrgBank(bank, value);
        }
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        switch (bank) {
            case 0: {
                final int val = ((value >> 1) ^ reg1) << 1;
                super.setChrBank(0, val);
                super.setChrBank(1, val | 1);
                break;
            }
            case 2: {
                final int val = ((value >> 1) | ((reg2 & 0x40) << 1)) << 1;
                super.setChrBank(2, val);
                super.setChrBank(3, val | 1);
                break;
            }
            case 4: {
                final int val = (reg2 & 0x3F) << 2;
                super.setChrBank(4, val);
                super.setChrBank(5, val | 1);
                super.setChrBank(6, val | 2);
                super.setChrBank(7, val | 3);
                break;
            }
        }
    }

    @Override
    public int readMemory(final int address) {
        if ((0xF000 & address) == 0x6000) {
            if (!getBitBool(address, 0)) {
                if ((reg0 & 0xE0) == 0xC0) {
                    reg1 = super.readMemory(0x006A);
                } else {
                    reg2 = super.readMemory(0x00FF);
                }
                updatePrgBanks();
                setChrBank(0, R[0] & 0xFE);
                setChrBank(1, R[0] | 0x01);
                setChrBank(2, R[1] & 0xFE);
                setChrBank(3, R[1] | 0x01);
                setChrBank(4, R[2]);
                setChrBank(5, R[3]);
                setChrBank(6, R[4]);
                setChrBank(7, R[5]);
            }
            return 0;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((0xF000 & address) == 0x6000) {
            if (!getBitBool(address, 0)) {
                reg0 = value;
                updateBanks();
            }
        } else {
            super.writeMemory(address, value);
        }
    }
}