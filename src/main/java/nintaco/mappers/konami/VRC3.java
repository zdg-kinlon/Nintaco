package nintaco.mappers.konami;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class VRC3 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private int irqLatch;
    private boolean irqEnableOnAck;
    private boolean irqEnabled;
    private boolean irq8bit;

    public VRC3(final CartFile cartFile) {
        super(cartFile, 4, 0);
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
                irqLatch = (irqLatch & 0xFFF0) | (value & 0x0F);
                break;
            case 0x9000:
                irqLatch = (irqLatch & 0xFF0F) | ((value & 0x0F) << 4);
                break;
            case 0xA000:
                irqLatch = (irqLatch & 0xF0FF) | ((value & 0x0F) << 8);
                break;
            case 0xB000:
                irqLatch = (irqLatch & 0x0FFF) | ((value & 0x0F) << 12);
                break;
            case 0xC000:
                writeIrqControl(value);
                break;
            case 0xD000:
                writeIrqAcknowledge();
                break;
            case 0xF000:
                setPrgBank(2, value & 7);
                break;
        }
    }

    private void writeIrqControl(final int value) {
        cpu.interrupt().setMapperIrq(false);

        irqEnableOnAck = getBitBool(value, 0);
        irqEnabled = getBitBool(value, 1);
        irq8bit = getBitBool(value, 2);

        if (irqEnabled) {
            irqCounter = irqLatch;
        }
    }

    private void writeIrqAcknowledge() {
        cpu.interrupt().setMapperIrq(false);
        irqEnabled = irqEnableOnAck;
    }

    @Override
    public void update() {
        if (irqEnabled) {
            irqCounter++;
            if (irq8bit) {
                if ((irqCounter & 0xFF) == 0) {
                    irqCounter = irqLatch;
                    cpu.interrupt().setMapperIrq(true);
                }
            } else {
                if ((irqCounter & 0xFFFF) == 0) {
                    irqCounter = irqLatch;
                    cpu.interrupt().setMapperIrq(true);
                }
            }
        }
    }
}
