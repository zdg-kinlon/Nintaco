package nintaco.mappers.konami;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public abstract class VrcIrq extends Mapper {

    private static final long serialVersionUID = 0;

    protected int irqLatch;
    protected boolean irqCycleMode;
    protected boolean irqEnabled;
    protected boolean irqEnableAfterAck;
    protected int irqCounter;
    protected float scanlineCycleCounter = 341;
    protected float scanlineDelta;

    public VrcIrq(final CartFile cartFile) {
        this(cartFile, 8, 8);
    }

    public VrcIrq(final CartFile cartFile, final int prgBanksSize,
                  final int chrBanksSize) {
        super(cartFile, prgBanksSize, chrBanksSize);
    }

    @Override
    public void init() {
        scanlineDelta = ntsc ? 3 : (float) (16.0 / 5.0);
    }

    protected void writeIrqLatchLow(final int value) {
        irqLatch = (irqLatch & 0xF0) | (value & 0x0F);
    }

    protected void writeIrqLatchHigh(final int value) {
        irqLatch = (irqLatch & 0x0F) | ((value & 0x0F) << 4);
    }

    protected void writeIrqLatch(final int value) {
        irqLatch = value;
    }

    protected void writeIrqControl(final int value) {
        irqEnableAfterAck = getBitBool(value, 0);
        irqEnabled = getBitBool(value, 1);
        irqCycleMode = getBitBool(value, 2);

        cpu.setMapperIrq(false);
        if (irqEnabled) {
            irqCounter = irqLatch;
            scanlineCycleCounter = 341;
        }
    }

    protected void writeIrqAcknowledge() {
        cpu.setMapperIrq(false);
        irqEnabled = irqEnableAfterAck;
    }

    protected void clockIrqCounter() {
        if (irqCounter == 0xFF) {
            irqCounter = irqLatch;
            cpu.setMapperIrq(true);
        } else {
            irqCounter++;
        }
    }

    @Override
    public void update() {
        if (irqEnabled) {
            if (irqCycleMode) {
                clockIrqCounter();
            } else {
                scanlineCycleCounter -= scanlineDelta;
                if (scanlineCycleCounter <= 0) {
                    scanlineCycleCounter += 341;
                    clockIrqCounter();
                }
            }
        }
    }
}