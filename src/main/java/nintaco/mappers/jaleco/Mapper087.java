package nintaco.mappers.jaleco;

import nintaco.files.NesFile;
import nintaco.mappers.nintendo.CNROM;

public class Mapper087 extends CNROM {

    private static final long serialVersionUID = 0;

    public Mapper087(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        memory[address] = value;
        if ((address & 0xE000) == 0x6000) {
            setChrBank(0, ((value & 1) << 1) | ((value & 2) >> 1));
        }
    }
}
