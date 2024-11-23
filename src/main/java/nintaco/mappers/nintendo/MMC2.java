package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class MMC2 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[][] chrRegs = new int[2][2];
    private final int[] latches = new int[2];

    public MMC2(final CartFile cartFile) {
        super(cartFile, 8, 2);
    }

    @Override
    public void init() {
        setPrgBank(5, -3);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public int readVRAM(final int address) {
        final int value;
        if (address < 0x2000) {
            final int index = address >> 12;
            value = super.readVRAM(address);
            switch (address & 0x0FF0) {
                case 0x0FD0:
                    latches[index] = 0;
                    updateChrBanks();
                    break;
                case 0x0FE0:
                    latches[index] = 1;
                    updateChrBanks();
                    break;
            }
        } else {
            value = vram[address];
        }
        return value;
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0xA000) {
            switch (address & 0xF000) {
                case 0xA000:
                    setPrgBank(4, value);
                    break;
                case 0xB000:
                    writeChrReg(0, 0, value);
                    break;
                case 0xC000:
                    writeChrReg(0, 1, value);
                    break;
                case 0xD000:
                    writeChrReg(1, 0, value);
                    break;
                case 0xE000:
                    writeChrReg(1, 1, value);
                    break;
                case 0xF000:
                    setNametableMirroring(value & 1);
                    break;
            }
        }
    }

    private void updateChrBanks() {
        setChrBank(0, chrRegs[0][latches[0]]);
        setChrBank(1, chrRegs[1][latches[1]]);
    }

    private void writeChrReg(final int i, final int j, final int value) {
        chrRegs[i][j] = value;
        updateChrBanks();
    }
}
