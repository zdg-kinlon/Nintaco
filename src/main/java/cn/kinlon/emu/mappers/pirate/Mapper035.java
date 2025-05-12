package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper035 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private boolean irqEnabled;
    private boolean lastA12;

    public Mapper035(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        irqCounter = 0;
        irqEnabled = false;
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF007) {
            case 0x8000:
            case 0x8001:
            case 0x8002:
            case 0x8003:
                setPrgBank(4 | (address & 3), value);
                break;

            case 0x9000:
            case 0x9001:
            case 0x9002:
            case 0x9003:
            case 0x9004:
            case 0x9005:
            case 0x9006:
            case 0x9007:
                setChrBank(address & 7, value);
                break;

            case 0xC002:
                irqEnabled = false;
                cpu.setMapperIrq(false);
                break;

            case 0xC003:
                irqEnabled = true;
                break;
            case 0xC005:
                irqCounter = value;
                break;

            case 0xD001:
                setNametableMirroring(value & 1);
                break;
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        final boolean a12 = (address & 0x1000) != 0;
        if (irqEnabled && !lastA12 && a12) {
            irqCounter = (irqCounter - 1) & 0xFF;
            if (irqCounter == 0) {
                irqEnabled = false;
                cpu.setMapperIrq(true);
            }
        }
        lastA12 = a12;
    }
}