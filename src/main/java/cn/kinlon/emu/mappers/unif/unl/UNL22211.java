package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class UNL22211 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    public UNL22211(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        updateBanks();
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x4100) {
            return (regs[1] ^ regs[2]) | 0x40;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xFFFC) == 0x4100) {
            regs[address & 3] = value;
        } else {
            super.writeMemory(address, value);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        updateBanks();
    }

    public void updateBanks() {
        setPrgBank((regs[2] >> 2) & 1);
        setChrBank(regs[2] & 3);
    }
}