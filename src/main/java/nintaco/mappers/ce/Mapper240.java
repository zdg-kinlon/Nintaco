package nintaco.mappers.ce;

import nintaco.files.NesFile;
import nintaco.mappers.nintendo.GxROM;

public class Mapper240 extends GxROM {

    private static final long serialVersionUID = 0;

    public Mapper240(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x4020 && address <= 0x5FFF) {
            prgBanks[1] = (value & 0xF0) << 11;
            setChrBank(0, value & 0x0F);
        } else {
            memory[address] = value;
        }
    }
}
