package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class DREAMTECH01 extends Mapper {

    private int register;

    public DREAMTECH01(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        register = 0;
        updateBanks();
        setChrBank(0);
    }

    private void updateBanks() {
        setPrgBank(2, register);
        setPrgBank(3, 8);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address == 0x5020) {
            register = value & 7;
            updateBanks();
        }
        memory[address] = value;
    }
}
