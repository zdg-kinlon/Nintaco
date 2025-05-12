package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper040 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean irqEnabled;
    private int irqCounter;

    public Mapper040(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
        setPrgBank(3, 0x06);
        setPrgBank(4, 0x04);
        setPrgBank(5, 0x05);
        setPrgBank(7, 0x07);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xE000) {
            case 0x8000:
                disableIrq();
                break;
            case 0xA000:
                irqEnabled = true;
                break;
            case 0xE000:
                setPrgBank(6, value & 7);
                break;
        }
    }

    private void disableIrq() {
        irqEnabled = false;
        cpu.setMapperIrq(false);
        irqCounter = 0;
    }

    @Override
    public void update() {
        if (irqEnabled && ++irqCounter == 0x1000) {
            cpu.setMapperIrq(true);
            irqEnabled = false;
            irqCounter = 0;
        }
    }
}
