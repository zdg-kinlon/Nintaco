package nintaco.mappers.henggedianzi;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Henggedianzi179 extends Mapper {

    private static final long serialVersionUID = 0;

    public Henggedianzi179(final CartFile cartFile) {
        super(cartFile, 2, 1, 0x5000, 0x8000);
    }

    @Override
    public void init() {
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0xF000) == 0x5000) {
            setPrgBank(value >> 1);
        } else if (address >= 0x8000) {
            setNametableMirroring(value & 1);
        }
    }
}
