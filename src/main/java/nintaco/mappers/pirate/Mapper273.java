package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static java.util.Arrays.fill;
import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class Mapper273 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] prgRegs = new int[2];
    private final int[] chrRegs = new int[8];

    private int mirroring;
    private int prescalerMask;
    private int prescaler;
    private int irqCounter;
    private boolean irqEnabled;

    public Mapper273(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        fill(prgRegs, 0);
        fill(chrRegs, 0);
        mirroring = prescalerMask = prescaler = irqCounter = 0;
        irqEnabled = false;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        setPrgBank(4, prgRegs[0]);
        setPrgBank(5, prgRegs[1]);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
        for (int i = 7; i >= 0; --i) {
            setChrBank(i, chrRegs[i]);
        }
        setNametableMirroring(mirroring);
    }

    @Override
    public void update() {
        if (irqEnabled) {
            ++prescaler;
            prescaler &= 0xFF;
            if ((prescaler & prescalerMask) == 0x00) {
                prescalerMask = 0xFF;
                ++irqCounter;
                irqCounter &= 0xFF;
                cpu.setMapperIrq(irqCounter == 0);
            }
        }
    }

    private void writeIRQ(final int address, final int value) {
        switch (address & 8) {
            case 0:
                irqCounter = value;
                cpu.setMapperIrq(false);
                break;
            case 8:
                if ((value & 1) == 0) {
                    irqEnabled = false;
                    prescaler = 0;
                    prescalerMask = 0x7F;
                    cpu.setMapperIrq(false);
                } else {
                    irqEnabled = true;
                }
                break;
        }
    }

    private void writeMirroring(final int value) {
        mirroring = value & 1;
        updateState();
    }

    private void writePrgRegs(final int address, final int value) {
        prgRegs[getBit(address, 13)] = value;
        updateState();
    }

    private void writeChrRegs(final int address, final int value) {
        final int reg = (((address >> 12) - 0xB) << 1) | ((address & 8) >> 3);
        if (getBitBool(address, 2)) {
            chrRegs[reg] = (value << 4) | (chrRegs[reg] & 0x0F);
        } else {
            chrRegs[reg] = (chrRegs[reg] & 0xF0) | (value & 0x0F);
        }
        updateState();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (0xF000 & address) {
            case 0x8000:
            case 0xA000:
                writePrgRegs(address, value);
                break;
            case 0x9000:
                writeMirroring(value);
                break;
            case 0xB000:
            case 0xC000:
            case 0xD000:
            case 0xE000:
                writeChrRegs(address, value);
                break;
            case 0xF000:
                writeIRQ(address, value);
                break;
        }
    }
}
