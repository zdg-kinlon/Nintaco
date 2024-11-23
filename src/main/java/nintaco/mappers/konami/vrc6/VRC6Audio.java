package nintaco.mappers.konami.vrc6;

import nintaco.mappers.Audio;

public final class VRC6Audio extends Audio {

    private static final long serialVersionUID = 0;

    private static final float[] audioScale = new float[61];

    static {
        setVolume(100);
    }

    private final VrcPulseGenerator pulse1 = new VrcPulseGenerator();
    private final VrcPulseGenerator pulse2 = new VrcPulseGenerator();
    private final VrcSawtoothGenerator sawtooth = new VrcSawtoothGenerator();

    public static void setVolume(final int volume) {
        for (int i = 0; i < audioScale.length; i++) {
            audioScale[i] = i * volume * 253.14f / (audioScale.length - 1);
        }
    }

    @Override
    public void reset() {
        pulse1.reset();
        pulse2.reset();
        sawtooth.reset();
    }

    @Override
    public boolean writeRegister(int address, int value) {
        switch (address) {
            case 0x9000:
                pulse1.writeControl(value);
                return true;
            case 0x9001:
                pulse1.writeFrequencyLow(value);
                return true;
            case 0x9002:
                pulse1.writeFrequencyHigh(value);
                return true;
            case 0x9003:
                pulse1.writeFrequencyControl(value);
                pulse2.writeFrequencyControl(value);
                sawtooth.writeFrequencyControl(value);
                return true;
            case 0xA000:
                pulse2.writeControl(value);
                return true;
            case 0xA001:
                pulse2.writeFrequencyLow(value);
                return true;
            case 0xA002:
                pulse2.writeFrequencyHigh(value);
                return true;
            case 0xB000:
                sawtooth.writeAccumulatorRate(value);
                return true;
            case 0xB001:
                sawtooth.writeFrequencyLow(value);
                return true;
            case 0xB002:
                sawtooth.writeFrequencyHigh(value);
                return true;
            default:
                return false;
        }
    }

    @Override
    public float getAudioSample() {
        return audioScale[pulse1.getValue() + pulse2.getValue()
                + sawtooth.getValue()];
    }

    @Override
    public int getAudioMixerScale() {
        return 40221;
    }

    @Override
    public void update() {
        pulse1.update();
        pulse2.update();
        sawtooth.update();
    }
}