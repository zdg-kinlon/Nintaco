package nintaco.mappers.txc;

// TODO STRIKE WOLF NOT WORKING CORRRECTLY

import nintaco.files.*;

public class Txc22000 extends TxcLatch {

    private static final long serialVersionUID = 0;

    private int chr;

    public Txc22000(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void updateState() {
        setPrgBank(output & 3);
        setChrBank(chr);
    }

    @Override
    public int readMemory(final int address) {
        return ((address & 0xE000) == 0x4000)
                ? (0xCF | ((readLatch(address) << 4) & 0x30))
                : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x4000) {
            if ((address & 0xF200) == 0x4200) {
                chr = value;
            }
            writeLatch(address, (value >> 4) & 3);
        } else {
            memory[address] = value;
        }
    }
}
