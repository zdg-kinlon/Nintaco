package nintaco.mappers.kaiser;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Kaiser106C extends Mapper {

    private static final long serialVersionUID = 0;

    private int bank;

    public Kaiser106C(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(bank);
        setChrBank(bank);
        setNametableMirroring((bank & 1) ^ 1);
    }

    @Override
    public void resetting() {
        bank = (bank + 1) & 3;
        init();
    }
}