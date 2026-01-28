package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper222 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private int irqResetDelay;

    public Mapper222(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (irqResetDelay > 0) {
            irqResetDelay--;
        }

        final boolean a12 = (address & 0x1000) != 0;
        if (a12 && irqResetDelay == 0 && irqCounter != 0 && ++irqCounter >= 240) {
            cpu.interrupt().setMapperIrq(true);
            irqCounter = 0;
        }
        if (a12) {
            irqResetDelay = 8;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF003) {
            case 0x8000:
                setPrgBank(4, value);
                break;
            case 0x9000:
                setNametableMirroring(value & 1);
                break;
            case 0xA000:
                setPrgBank(5, value);
                break;
            case 0xB000:
                setChrBank(0, value);
                break;
            case 0xB002:
                setChrBank(1, value);
                break;
            case 0xC000:
                setChrBank(2, value);
                break;
            case 0xC002:
                setChrBank(3, value);
                break;
            case 0xD000:
                setChrBank(4, value);
                break;
            case 0xD002:
                setChrBank(5, value);
                break;
            case 0xE000:
                setChrBank(6, value);
                break;
            case 0xE002:
                setChrBank(7, value);
                break;
            case 0xF000:
                irqCounter = value;
                cpu.interrupt().setMapperIrq(false);
                break;
        }
    }
}
