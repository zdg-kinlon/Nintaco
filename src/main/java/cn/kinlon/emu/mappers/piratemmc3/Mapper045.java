package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper045 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    private int regIndex;
    private boolean sramRegisterEnabled;

    public Mapper045(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        sramRegisterEnabled = true;
        regIndex = 0;
        regs[2] = 0x0F;
        updateBanks();
    }

    @Override
    public void resetting() {
    }

    @Override
    public void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, (value & (0xFF >> (0x0F - (regs[2] & 0x0F))))
                | regs[0] | ((regs[2] & 0xF0) << 4));
    }

    @Override
    public void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, (value & (0x3F ^ (regs[3] & 0x3F))) | regs[1]);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address < 0x8000) {
            if (sramRegisterEnabled && address >= 0x6000) {
                if (getBitBool(regs[3], 6)) {
                    sramRegisterEnabled = false;
                } else {
                    regs[regIndex] = value;
                    regIndex = (regIndex + 1) & 3;
                }
                updateBanks();
            }
        } else {
            super.writeRegister(address, value);
        }
    }
}
