package nintaco.mappers.namco;

// TODO GUI ENABLE/DISABLE Namco163Audio

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper330 extends Mapper {

    private static final long serialVersionUID = 0;

    private final Namco163Audio audio = new Namco163Audio();

    private int irqCounter;

    public Mapper330(final CartFile cartFile) {
        super(cartFile, 8, 8, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        setPrgBank(3, 0);
        setPrgBank(4, 0);
        setPrgBank(5, 1);
        setPrgBank(6, 2);
        setPrgBank(7, -1);
    }

    @Override
    public void update() {
        audio.update();
        if ((irqCounter & 0x8000) != 0) {
            ++irqCounter;
            irqCounter &= 0xFFFF;
            if (irqCounter == 0) {
                cpu.setMapperIrq(true);
            }
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (!audio.writeRegister(address, value)) {
            super.writeMemory(address, value);
        }
    }

    private void writeChrAndIrq(final int address, final int value) {
        if ((address & 0x4400) == 0x0400) {
            if (getBitBool(address, 13)) {
                irqCounter = (value << 8) | (irqCounter & 0x00FF);
                cpu.setMapperIrq(false);
            } else {
                irqCounter = (irqCounter & 0xFF00) | value;
            }
        } else {
            setChrBank((address >> 11) & 7, value);
        }
    }

    private void writeNametableMirroring(final int address, final int value) {
        setNametable((address >> 11) & 3, value & 1);
    }

    private void writePrg(final int address, final int value) {
        final int bank = 4 | ((address >> 11) & 3);
        if (bank != 7) {
            setPrgBank(bank, value);
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {
        switch (address >> 13) {
            case 4:
            case 5:
                writeChrAndIrq(address, value);
                break;
            case 6:
                writeNametableMirroring(address, value);
                break;
            case 7:
                writePrg(address, value);
                break;
        }
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