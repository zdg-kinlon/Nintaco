package nintaco.mappers.txc;

import nintaco.files.*;

public class Txc22211C extends TxcLatch {

    private static final long serialVersionUID = 0;

    private boolean ppuDisabled;

    public Txc22211C(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        super.init();
        ppuDisabled = false;
    }

    @Override
    protected void updateState() {
        setPrgBank(0);
        if (chrRomLength >= 0x4000) {
            ppuDisabled = false;
            setChrBank(((output & 2) << 1) | (Y ? 2 : 0) | (output & 1));
        } else {
            // 8 KiB UVEPROM, A14 becomes /PGM, i.e. CHR disable
            if (Y) {
                ppuDisabled = false;
                setChrBank(0);
            } else {
                ppuDisabled = true;
            }
        }
    }

    @Override
    public int readVRAM(final int address) {
        return ppuDisabled ? 0xFF : super.readVRAM(address);
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (!ppuDisabled) {
            super.writeVRAM(address, value);
        }
    }

    @Override
    public int readMemory(final int address) {
        return ((address & 0xE000) == 0x4000)
                ? (0xF0 | (readLatch(address) & 0x0F)) : super.readMemory(address);
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