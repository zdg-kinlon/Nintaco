package nintaco.mappers.frontfareast;

import nintaco.files.NesFile;

public class Mapper017 extends FrontFareast {

    private static final long serialVersionUID = 0;

    public Mapper017(final NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void init() {
        super.init();
        setPrgBanks(4, 4, -4);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xFFFC) == 0x4504) {
            setPrgBank(address & 7, value);
        } else if ((address & 0xFFF8) == 0x4510) {
            setChrBank(address & 7, value);
        }
        super.writeMemory(address, value);
    }
}
