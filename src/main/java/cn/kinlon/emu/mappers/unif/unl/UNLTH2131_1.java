package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.konami.VRC2And4;


public class UNLTH2131_1 extends VRC2And4 {

    protected int irqLowCounter;
    protected int irqHighCounter;

    public UNLTH2131_1(final CartFile cartFile) {
        super(cartFile);
        prgHigh = 0x20;
        variant = VRC2b;
        useHeuristics = false;
    }

    private void writeIrqAcknowledgeAndReset() {
        cpu.setMapperIrq(false);
        irqEnabled = false;
        irqLowCounter = 0;
    }

    private void writeIrqCounterEnable() {
        irqEnabled = true;
    }

    private void writeIrqHighCounterValue(final int value) {
        irqHighCounter = value >> 4;
    }

    @Override
    protected void writeRegister(int address, final int value) {
        switch (address & 0xF003) {
            case 0xF000:
                writeIrqAcknowledgeAndReset();
                break;
            case 0xF001:
                writeIrqCounterEnable();
                break;
            case 0xF002:
                break;
            case 0xF003:
                writeIrqHighCounterValue(value);
                break;
            default:
                super.writeRegister(address, value);
                break;
        }
    }

    @Override
    public void update() {
        if (irqEnabled) {
            ++irqLowCounter;
            irqLowCounter &= 0xFFF;
            if (irqLowCounter == 0x800) {
                --irqHighCounter;
                irqHighCounter &= 0xFF;
            }
            if (irqHighCounter == 0 && irqLowCounter < 0x800) {
                cpu.setMapperIrq(true);
            }
        }
    }
}