package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class H2288 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] REGISTERS = {0, 3, 1, 5, 6, 7, 2, 4};

    private int reg;

    public H2288(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF801) == 0x5800) {
            reg = value;
            updateBanks();
        } else {
            super.writeMemory(address, value);
        }
    }

    @Override
    protected void writeBankSelect(final int value) {
        super.writeBankSelect((value & 0xF8) | REGISTERS[value & 0x07]);
    }

    @Override
    protected void updateBanks() {
        if (getBitBool(reg, 6)) {
            int bank = ((reg & 0x05) | ((reg & 0x08) >> 2)
                    | ((reg & 0x20) >> 2)) << 1;
            if (getBitBool(reg, 1)) {
                bank &= 0xFC;
                setPrgBank(0x04, bank);
                setPrgBank(0x05, bank | 1);
                setPrgBank(0x06, bank | 2);
                setPrgBank(0x07, bank | 3);
            } else {
                bank &= 0xFE;
                setPrgBank(0x04, bank);
                setPrgBank(0x05, bank | 1);
                setPrgBank(0x06, bank);
                setPrgBank(0x07, bank | 1);
            }
        } else {
            updatePrgBanks();
        }
        updateChrBanks();
    }
}