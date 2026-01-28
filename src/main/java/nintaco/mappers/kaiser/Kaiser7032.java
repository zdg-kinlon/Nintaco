package nintaco.mappers.kaiser;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Kaiser7032 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    private int irqReloadValue;
    private int irqCounter;
    private int regIndex;
    private boolean irqEnabled;

    public Kaiser7032(final CartFile cartFile) {
        super(cartFile, 8, 8, 0x8000, 0x6000);
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
                irqReloadValue = (irqReloadValue & 0xFFF0) | (value & 0x0F);
                break;
            case 0x9000:
                irqReloadValue = (irqReloadValue & 0xFF0F) | ((value & 0x0F) << 4);
                break;
            case 0xA000:
                irqReloadValue = (irqReloadValue & 0xF0FF) | ((value & 0x0F) << 8);
                break;
            case 0xB000:
                irqReloadValue = (irqReloadValue & 0x0FFF) | ((value & 0x0F) << 12);
                break;

            case 0xC000:
                irqEnabled = (value != 0);
                if (irqEnabled) {
                    irqCounter = irqReloadValue;
                }
                cpu.interrupt().setMapperIrq(false);
                break;

            case 0xD000:
                cpu.interrupt().setMapperIrq(false);
                break;
            case 0xE000:
                regIndex = (value & 0x0F) - 1;
                break;

            case 0xF000:
                if (regIndex <= 2) {
                    regs[regIndex] = ((regs[regIndex]) & 0x10) | (value & 0x0F);
                } else if (regIndex == 3) {
                    regs[3] = value;
                }

                switch (address & 0xFC00) {
                    case 0xF000: {
                        final int bank = address & 0x03;
                        if (bank < 3) {
                            regs[bank] = (value & 0x10) | (regs[bank] & 0x0F);
                        }
                        break;
                    }

                    case 0xF800:
                        setNametableMirroring(value & 0x01);
                        break;

                    case 0xFC00:
                        setChrBank(address & 0x07, value);
                        break;
                }
                setPrgBank(3, regs[3]);
                setPrgBank(4, regs[0]);
                setPrgBank(5, regs[1]);
                setPrgBank(6, regs[2]);
                break;
        }
    }

    @Override
    public void update() {
        if (irqEnabled && ++irqCounter == 0xFFFF) {
            irqCounter = irqReloadValue;
            cpu.interrupt().setMapperIrq(true);
        }
    }
}