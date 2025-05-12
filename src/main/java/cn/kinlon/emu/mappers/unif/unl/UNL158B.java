package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;


import static cn.kinlon.emu.utils.BitUtil.*;

public class UNL158B extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] LUT
            = {0x00, 0x00, 0x00, 0x01, 0x02, 0x04, 0x0F, 0x00};

    private int reg;

    public UNL158B(final CartFile cartFile) {
        super(cartFile);
        prgBlockMask = 0x0F;
    }

    @Override
    public void init() {
        R[6] = 0;
        R[7] = 1;
        updateBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void updatePrgBanks() {
        if (getBitBool(reg, 7)) {
            int val = (reg & 7) << 1;
            if (getBitBool(reg, 5)) {
                val &= 0x0C;
                setPrgBank(4, val);
                setPrgBank(5, val | 1);
                setPrgBank(6, val | 2);
                setPrgBank(7, val | 3);
            } else {
                setPrgBank(4, val);
                setPrgBank(5, val | 1);
                setPrgBank(6, val);
                setPrgBank(7, val | 1);
            }
        } else {
            super.updatePrgBanks();
        }
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            return LUT[address & 7];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF007) == 0x5000) {
            reg = value;
            updatePrgBanks();
        }
        super.writeMemory(address, value);
    }
}
