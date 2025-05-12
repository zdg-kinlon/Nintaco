
package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class Mapper162 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    public Mapper162(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        regs[0] = 3;
        regs[1] = 0;
        regs[2] = 0;
        regs[3] = 7;

        setChrBank(0);
        updateBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateBanks() {
        switch (regs[3] & 5) {
            case 0:
                setPrgBank((regs[0] & 0x0C) | (regs[1] & 0x02)
                        | ((regs[2] & 0x0F) << 4));
                break;
            case 1:
                setPrgBank((regs[0] & 0x0C) | (regs[2] & 0x0F) << 4);
                break;
            case 4:
                setPrgBank((regs[0] & 0x0E) | ((regs[1] >> 1) & 0x01)
                        | ((regs[2] & 0x0F) << 4));
                break;
            case 5:
                setPrgBank((regs[0] & 0x0F) | ((regs[2] & 0x0F) << 4));
                break;
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xF000) == 0x5000) {
            regs[(address >> 8) & 3] = value;
            updateBanks();
        }
    }
}
