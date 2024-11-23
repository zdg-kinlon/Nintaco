package nintaco.mappers.magicseries;

import nintaco.files.NesFile;
import nintaco.mappers.nintendo.GxROM;

public class Mapper107 extends GxROM {

    private static final long serialVersionUID = 0;

    public Mapper107(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected void writeRegister(int address, int value) {
        setPrgBank(value >> 1);
        setChrBank(value);
    }
}
