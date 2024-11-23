package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class ShuiGuanPipe extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private int irqPre;
    private boolean irqEnabled;

    public ShuiGuanPipe(final CartFile cartFile) {
        super(cartFile, 8, 8, 0x6000, 0x6000);
    }

    @Override
    public void init() {
        irqPre = 0;
        irqCounter = 0;
        irqEnabled = false;
        for (int i = 7; i >= 0; i--) {
            setPrgBank(i, 0);
            setChrBank(i, 0);
        }
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0xF800) == 0x6800) {
            setPrgBank(3, address & 0x3F);
        } else if (((address & 0xF80C) >= 0xB000)
                && ((address & 0xF80C) <= 0xE00C)) {
            final int index = (((address >> 11) - 6) | (address >> 3)) & 7;
            chrBanks[index] = (((chrBanks[index] >> 10) & (0xF0 >> (address & 4)))
                    | ((value & 0x0F) << (address & 4))) << 10;
        } else {
            switch (address & 0xF80C) {
                case 0x8800:
                    setPrgBank(4, value);
                    break;
                case 0x9800:
                    setNametableMirroring(value & 3);
                    break;
                case 0xA800:
                    setPrgBank(5, value);
                    break;
                case 0xA000:
                    setPrgBank(6, value);
                    break;
                case 0xF000:
                    irqCounter = ((irqCounter & 0xF0) | (value & 0x0F));
                    break;
                case 0xF004:
                    irqCounter = ((irqCounter & 0x0F) | ((value & 0x0F) << 4));
                    break;
                case 0xF008:
                    irqEnabled = value != 0;
                    if (!irqEnabled) {
                        irqPre = 0;
                    }
                    cpu.setMapperIrq(false);
                    break;
                case 0xF00C:
                    irqPre = 16;
                    break;
            }
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (rendering && irqEnabled && scanlineCycle == 256) {
            irqCounter = (irqCounter + 1) & 0xFF;
            if ((irqCounter - irqPre) == 238) {
                cpu.setMapperIrq(true);
            }
        }
    }
}