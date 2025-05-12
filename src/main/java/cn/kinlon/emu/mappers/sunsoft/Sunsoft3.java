package cn.kinlon.emu.mappers.sunsoft;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Sunsoft3 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private boolean irqWriteLow;
    private boolean irqEnabled;

    public Sunsoft3(final CartFile cartFile) {
        super(cartFile, 4, 4);
    }

    @Override
    public void init() {
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF800) {
            case 0x8800:
                setChrBank(0, value);
                break;
            case 0x9800:
                setChrBank(1, value);
                break;
            case 0xA800:
                setChrBank(2, value);
                break;
            case 0xB800:
                setChrBank(3, value);
                break;
            case 0xC800:
                writeIrqLoad(value);
                break;
            case 0xD800:
                writeIrqEnable(value);
                break;
            case 0xE800:
                setNametableMirroring(value);
                break;
            case 0xF800:
                setPrgBank(2, value);
                break;
        }
    }

    private void writeIrqLoad(final int value) {
        if (irqWriteLow) {
            irqCounter = (irqCounter & 0xFF00) | value;
        } else {
            irqCounter = (value << 8) | (irqCounter & 0x00FF);
        }
        irqWriteLow = !irqWriteLow;
    }

    private void writeIrqEnable(final int value) {
        irqEnabled = getBitBool(value, 4);
        cpu.setMapperIrq(false);
        irqWriteLow = false;
    }

    @Override
    public void update() {
        if (irqEnabled) {
            irqCounter = (irqCounter - 1) & 0xFFFF;
            if (irqCounter == 0xFFFF) {
                cpu.setMapperIrq(true);
                irqEnabled = false;
            }
        }
    }
}
