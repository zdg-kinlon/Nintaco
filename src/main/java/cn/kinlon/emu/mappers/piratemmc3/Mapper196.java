package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class Mapper196 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean bankMode;
    private int bank;

    public Mapper196(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updatePrgBanks() {
        if (bankMode) {
            setPrgBanks(4, 4, bank);
        } else {
            super.updatePrgBanks();
        }
    }

    @Override
    public void writeMemory(int address, final int value) {
        memory[address] = value;
        if (address >= 0x6000) {
            if (address < 0x8000) {
                bankMode = true;
                bank = ((value & 0x0F) | (value >> 4)) << 2;
                updatePrgBanks();
            } else {
                if (address >= 0xC000) {
                    address = (address & 0xFFFE) | ((address >> 2) & 0x01)
                            | ((address >> 3) & 0x01);
                } else {
                    address = (address & 0xFFFE) | ((address >> 2) & 0x01)
                            | ((address >> 3) & 0x01) | ((address >> 1) & 0x01);
                }
                super.writeRegister(address, value);
            }
        }
    }
}
