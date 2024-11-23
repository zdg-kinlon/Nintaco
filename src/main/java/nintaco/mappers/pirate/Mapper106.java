package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper106 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private boolean irqEnabled;

    public Mapper106(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBank(4, -1);
        setPrgBank(5, -1);
        setPrgBank(6, -1);
        setPrgBank(7, -1);
    }

    @Override
    public void update() {
        if (irqEnabled) {
            irqCounter = (irqCounter + 1) & 0xFFFF;
            if (irqCounter == 0) {
                cpu.setMapperIrq(true);
                irqEnabled = false;
            }
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0x0F) {

            case 0x00:
            case 0x02:
                setChrBank(address & 7, value & 0xFE);
                break;

            case 0x01:
            case 0x03:
                setChrBank(address & 7, value | 0x01);
                break;

            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
                setChrBank(address & 7, value);
                break;

            case 0x08:
                setPrgBank(4, (value & 0x0F) | 0x10);
                break;

            case 0x09:
                setPrgBank(5, value & 0x1F);
                break;

            case 0x0A:
                setPrgBank(6, value & 0x1F);
                break;

            case 0x0B:
                setPrgBank(7, (value & 0x0F) | 0x10);
                break;

            case 0x0D:
                irqEnabled = false;
                irqCounter = 0;
                cpu.setMapperIrq(false);
                break;

            case 0x0E:
                irqCounter = (irqCounter & 0xFF00) | value;
                break;

            case 0x0F:
                irqCounter = (irqCounter & 0xFF) | (value << 8);
                irqEnabled = true;
                break;
        }
    }
}