package nintaco.mappers.jaleco;

import nintaco.files.NesFile;
import nintaco.mappers.Mapper;

public class Mapper092 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper092(NesFile nesFile) {
        super(nesFile, 4, 1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        int reg = value & 0xF0;
        int bank = value & 0x0F;
        if (value >= 0x9000) {
            switch (reg) {
                case 0xD0:
                    setPrgBank(3, value & 0x0F);
                    break;
                case 0xE0:
                    setChrBank(0, bank);
                    break;
            }
        } else {
            switch (reg) {
                case 0xB0:
                    setPrgBank(3, value & 0x0F);
                    break;
                case 0x70:
                    setChrBank(0, bank);
                    break;
            }
        }
    }
}
