package nintaco.mappers.unif.unl;

// TODO WIP

import nintaco.files.*;
import nintaco.mappers.sunsoft.fme7.*;

import static nintaco.util.BitUtil.*;

public class UNL831128C extends SunsoftFME7 {

    private static final long serialVersionUID = 0;

    private int irqControl;
    private int irqLatch;
    private int irqCycles;

    public UNL831128C(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void update() {
        if ((irqControl & 0x02) != 0
                && ((irqControl & 0x04) != 0 || ((irqCycles -= 3) <= 0))) {
            if ((irqControl & 0x04) == 0) {
                irqCycles += 341;
            }
            if (irqCounter == 0xFF) {
                irqCounter = irqLatch;
                cpu.interrupt().setMapperIrq(true);
            } else {
                ++irqCounter;
            }
        }
        audio.update();
    }

    private void writeIrqControl(final int value) {
        irqControl = value;
        if (getBitBool(irqControl, 1)) {
            irqCounter = irqLatch;
            irqCycles = 341;
        }
        cpu.interrupt().setMapperIrq(false);
    }

    private void writeIrqAcknowledge() {
        irqControl = setBit(irqControl, 1, getBitBool(irqControl, 0));
        cpu.interrupt().setMapperIrq(false);
    }

    private void writeIrqLatch(final int value) {
        irqLatch = value;
    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address & 0xF00F) {
            case 0xA00D:
            case 0xC00D:
                writeIrqControl(value);
                break;
            case 0xA00E:
            case 0xC00E:
                writeIrqAcknowledge();
                break;
            case 0xA00F:
            case 0xC00F:
                writeIrqLatch(value);
                break;
            default:
                super.writeMemory(address, value);
                break;
        }
    }
}
