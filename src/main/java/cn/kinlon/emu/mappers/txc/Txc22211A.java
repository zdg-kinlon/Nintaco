package cn.kinlon.emu.mappers.txc;

import cn.kinlon.emu.files.CartFile;


public class Txc22211A extends TxcLatch {

    private static final long serialVersionUID = 0;

    public Txc22211A(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void updateState() {
        setPrgBank((output >> 2) & 1);
        setChrBank(output & 3);
    }

    @Override
    public int readMemory(final int address) {
        return ((address & 0xE000) == 0x4000)
                ? (0xF0 | (readLatch(address) & 0x0F))
                : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x4000) {
            writeLatch(address, value & 0x0F);
        } else {
            memory[address] = value;
        }
    }
}