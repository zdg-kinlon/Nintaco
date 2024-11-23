package nintaco.mappers.unif.unl.dripgame;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class DripGame extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] attributeBits = {0x00, 0x55, 0xAA, 0xFF};

    private final DripGameAudio audio = new DripGameAudio();

    private int lastNametableAddress;
    private boolean sramWritesEnabled;
    private boolean extendedAttributesEnabled;

    private int irqLow;
    private int irqCounter;
    private boolean irqEnabled;

    public DripGame(final CartFile cartFile) {
        super(cartFile, 4, 4);
        setPrgBank(3, -1);
        xram = new int[0x0800];
    }

    @Override
    public int readVRAM(final int address) {

        if (address >= 0x2000 && address < 0x3F00) {
            if ((address & 0x03C0) == 0x03C0) {
                if (extendedAttributesEnabled) {
                    return xram[lastNametableAddress & 0x07FF];
                }
            } else {
                lastNametableAddress = address;
            }
        }

        return super.readVRAM(address);
    }

    @Override
    public int readMemory(final int address) {
        switch (address & 0xF800) {
            case 0x4800:
                return 0x64;
            case 0x5000:
                return audio.getStatus(0);
            case 0x5800:
                return audio.getStatus(1);
            default:
                return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) != 0x6000 || sramWritesEnabled) {
            super.writeMemory(address, value);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (!audio.writeRegister(address, value)) {
            if (address < 0xC000) {
                switch (address & 0x000F) {
                    case 0x0008:
                        writeLowIrqCounter(value);
                        break;
                    case 0x0009:
                        writeHighIrqCounter(value);
                        break;
                    case 0x000A:
                        writeControl(value);
                        break;
                    case 0x000B:
                        setPrgBank(2, value & 0x0F);
                        break;
                    case 0x000C:
                        setChrBank(0, value & 0x0F);
                        break;
                    case 0x000D:
                        setChrBank(1, value & 0x0F);
                        break;
                    case 0x000E:
                        setChrBank(2, value & 0x0F);
                        break;
                    case 0x000F:
                        setChrBank(3, value & 0x0F);
                        break;
                }
            } else {
                writeExtendedAttributes(address, value);
            }
        }
    }

    private void writeLowIrqCounter(final int value) {
        irqLow = value;
    }

    private void writeHighIrqCounter(final int value) {
        irqCounter = ((value & 0x7F) << 8) | irqLow;
        irqEnabled = getBitBool(value, 7);
        cpu.setMapperIrq(false);
    }

    private void writeControl(final int value) {
        setNametableMirroring(value & 3);
        extendedAttributesEnabled = getBitBool(value, 2);
        sramWritesEnabled = getBitBool(value, 3);
    }

    private void writeExtendedAttributes(final int address, final int value) {
        xram[address & 0x07FF] = attributeBits[value & 3];
    }

    @Override
    public void update() {
        if (irqEnabled && irqCounter > 0 && --irqCounter == 0) {
            cpu.setMapperIrq(true);
        }
        audio.update();
    }

    @Override
    public int getAudioMixerScale() {
        return audio.getAudioMixerScale();
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }
}
