package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class MMC4 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[][] chrRegs = new int[2][2];

    public MMC4(final CartFile cartFile) {
        super(cartFile, 4, 2);
        setPrgBank(3, -1);
    }

    @Override
    public int readVRAM(final int address) {

        final int value = super.readVRAM(address);

        switch (address & 0xFFF8) {
            case 0x0FD8:
                updateChrBank(0, 0);
                break;
            case 0x0FE8:
                updateChrBank(0, 1);
                break;
            case 0x1FD8:
                updateChrBank(1, 0);
                break;
            case 0x1FE8:
                updateChrBank(1, 1);
                break;
        }

        return value;
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0xA000:
                setPrgBank(2, value);
                break;
            case 0xB000:
                writeChrBank(0, 0, value);
                break;
            case 0xC000:
                writeChrBank(0, 1, value);
                break;
            case 0xD000:
                writeChrBank(1, 0, value);
                break;
            case 0xE000:
                writeChrBank(1, 1, value);
                break;
            case 0xF000:
                setNametableMirroring(value & 1);
                break;
        }
    }

    private void writeChrBank(final int bank, final int latch, final int value) {
        chrRegs[bank][latch] = value & 0x1F;
        updateChrBank(bank, latch);
    }

    private void updateChrBank(final int bank, final int latch) {
        setChrBank(bank, chrRegs[bank][latch]);
    }
}
