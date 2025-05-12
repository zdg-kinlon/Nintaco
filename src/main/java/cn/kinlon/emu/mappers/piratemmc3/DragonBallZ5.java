package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

public class DragonBallZ5 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] chrBlocks = new int[2];

    public DragonBallZ5(final CartFile cartFile) {
        super(cartFile);
        mmc3a = true;
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x4020 && address < 0x6000) {
            writeChrBlockSelect(value);
        } else {
            super.writeMemory(address, value);
        }
    }

    protected void writeChrBlockSelect(int value) {
        chrBlocks[0] = (value & 0x01) << 18;
        chrBlocks[1] = (value & 0x10) << 14;
        updateChrBanks();
    }

    @Override
    protected void updateChrBanks() {
        super.updateChrBanks();
        for (int i = 0; i < 8; i++) {
            chrBanks[i] |= chrBlocks[i >> 2];
        }
    }
}
