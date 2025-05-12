package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Gyruss extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] regOrder = {4, 5, 6, 7, 0, 1, 2, 3};

    private final int[] regs = new int[8];

    public Gyruss(final CartFile cartFile) {
        super(cartFile, 32, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setChrBank(0);
        for (int i = 20; i <= 31; i++) {
            setPrgBank(i, 0x20 + i);
        }
        updateBanks();
    }

    public void updateBanks() {
        for (int i = 7; i >= 0; i--) {
            setPrgBank(i + 12, regs[regOrder[i]]);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0xA000) {
            setNametableMirroring((value & 1) ^ 1);
        } else if (address >= 0xB000 && address < 0xF000) {
            final int regIndex = ((address - 0xB000) >> 11) | ((address >> 1) & 1);
            if ((address & 1) != 0) {
                regs[regIndex] = (regs[regIndex] & 0x0F) | (value << 4);
            } else {
                regs[regIndex] = (regs[regIndex] & 0xF0) | (value & 0x0F);
            }
            updateBanks();
        }
    }
}