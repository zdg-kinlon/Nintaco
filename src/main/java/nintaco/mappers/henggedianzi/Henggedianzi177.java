package nintaco.mappers.henggedianzi;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;

public class Henggedianzi177 extends Mapper {

    private static final long serialVersionUID = 0;

    public Henggedianzi177(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(0);
        setChrBank(0);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setPrgBank(value);
        setNametableMirroring(getBit(value, 5));
    }
}
