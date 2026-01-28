package nintaco.mappers.irem;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class H3001 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean irqEnabled;
    private int irqReloadValue;
    private int irqCounter;

    public H3001(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBank(4, 0x00);
        setPrgBank(5, 0x01);
        setPrgBank(6, 0xFE);
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address) {
            case 0x8000:
                setPrgBank(4, value);
                break;
            case 0x9001:
                writeNametableMirroring(value);
                break;
            case 0x9003:
                writeIrqEnable(value);
                break;
            case 0x9004:
                writeReloadIrqCounter();
                break;
            case 0x9005:
                writeHighIrqReloadValue(value);
                break;
            case 0x9006:
                writeLowIrqReloadValue(value);
                break;
            case 0xA000:
                setPrgBank(5, value);
                break;
            case 0xB000:
            case 0xB001:
            case 0xB002:
            case 0xB003:
            case 0xB004:
            case 0xB005:
            case 0xB006:
            case 0xB007:
                setChrBank(address & 7, value);
                break;
            case 0xC000:
                setPrgBank(6, value);
                break;
        }
    }

    private void writeNametableMirroring(final int value) {
        setNametableMirroring(getBit(value, 7));
    }

    private void writeIrqEnable(final int value) {
        irqEnabled = getBitBool(value, 7);
        cpu.interrupt().setMapperIrq(false);
    }

    private void writeReloadIrqCounter() {
        irqCounter = irqReloadValue;
        cpu.interrupt().setMapperIrq(false);
    }

    private void writeHighIrqReloadValue(final int value) {
        irqReloadValue = (irqReloadValue & 0x00FF) | (value << 8);
    }

    private void writeLowIrqReloadValue(final int value) {
        irqReloadValue = (irqReloadValue & 0xFF00) | value;
    }

    @Override
    public void update() {
        if (irqEnabled && irqCounter > 0 && --irqCounter == 0) {
            cpu.interrupt().setMapperIrq(true);
        }
    }
}