package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper050 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean irqEnabled;
    private int irqCounter;

    public Mapper050(final CartFile cartFile) {
        super(cartFile, 8, 0, 0x8000, 0x6000);
        setPrgBank(3, 0x0F);
        setPrgBank(4, 0x08);
        setPrgBank(5, 0x09);
        setPrgBank(7, 0x0B);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x4020 && address <= 0x5FFF) {
            if ((address & 0x4120) == 0x4020) {
                writePrgBank(value);
            } else {
                writeIrqEnable(value);
            }
        } else {
            memory[address] = value;
        }
    }

    private void writePrgBank(final int value) {
        setPrgBank(6, (value & 8) | ((value & 1) << 2) | ((value & 6) >> 1));
    }

    private void writeIrqEnable(final int value) {
        irqEnabled = getBitBool(value, 0);
        if (!irqEnabled) {
            disableIrq();
        }
    }

    private void disableIrq() {
        irqEnabled = false;
        cpu.interrupt().setMapperIrq(false);
        irqCounter = 0;
    }

    @Override
    public void update() {
        if (irqEnabled && ++irqCounter == 0x1000) {
            cpu.interrupt().setMapperIrq(true);
            irqEnabled = false;
            irqCounter = 0;
        }
    }
}