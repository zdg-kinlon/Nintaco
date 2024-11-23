package nintaco.mappers.nitra;

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.MMC3;

public class Mapper250 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper250(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address < 0x8000) {
            memory[address] = value;
        } else {
            switch (address & 0xE400) {
                case 0x8000:
                    writeBankSelect(address & 0xFF);
                    break;
                case 0x8400:
                    writeBankData(address & 0xFF);
                    break;
                case 0xA000:
                    writeMirroring(address & 0xFF);
                    break;
                case 0xA400:
                    writePrgRamProtect(address & 0xFF);
                    break;
                case 0xC000:
                    writeIrqLatch(address & 0xFF);
                    break;
                case 0xC400:
                    writeIrqReload();
                    break;
                case 0xE000:
                    writeIrqDisable();
                    break;
                case 0xE400:
                    writeIrqEnable();
                    break;
            }
        }
    }
}
