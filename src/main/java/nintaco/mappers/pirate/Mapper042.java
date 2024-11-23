package nintaco.mappers.pirate;

import nintaco.files.NesFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper042 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean irqEnabled;
    private int irqCounter;

    public Mapper042(NesFile nesFile) {
        super(nesFile, 8, 1, 0x8000, 0x6000);
        setPrgBank(4, -4);
        setPrgBank(5, -3);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        switch (address & 0xE003) {
            case 0x8000:
                setChrBank(value);
                break;
            case 0xE000:
                setPrgBank(3, value & 0x0F);
                break;
            case 0xE001:
                setNametableMirroring(getBit(value, 3));
                break;
            case 0xE002:
                writeIrqControl(value);
                break;
        }
    }

    private void writeIrqControl(int value) {
        irqEnabled = getBitBool(value, 1);
        if (!irqEnabled) {
            cpu.setMapperIrq(false);
            irqCounter = 0;
        }
    }

    @Override
    public void update() {
        if (irqEnabled && ++irqCounter == 24576) {
            cpu.setMapperIrq(true);
        }
    }
}
