package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper202 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper202(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, 0);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int bank = (address >> 1) & 7;
        setChrBank(bank);
        setPrgBank(2, bank);
        setPrgBank(3, (address & 0x09) == 0x09 ? (bank + 1) : bank);
        setNametableMirroring(address & 1);
    }
}
