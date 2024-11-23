package nintaco.mappers.nintendo.mmc5;

import nintaco.mappers.Audio;
import nintaco.tv.TVSystem;

import static nintaco.tv.TVSystem.NTSC;
import static nintaco.util.BitUtil.getBitBool;

public class MMC5Audio extends Audio {

    private static final long serialVersionUID = 0;

    private static final int MIX_RANGE = 0x6545;
    private static final double NTSC_FREQUENCY = 19687500.0 / 11.0;
    private static final double PAL_FREQUENCY = 53203425.0 / 32.0;
    private static final double AUDIO_UPDATE_FREQUENCY = 240.0;

    private static final float[] pulseTable = new float[31];
    private static final float[] pcmTable = new float[256];

    static {
        setVolume(100);
    }

    private final MMC5 mmc5;
    private final MMC5PulseGenerator pulse1 = new MMC5PulseGenerator();
    private final MMC5PulseGenerator pulse2 = new MMC5PulseGenerator();
    private float audioUpdateThreshold;
    private float audioUpdateTicks;
    private int multiplierA;
    private int multiplierB;
    private int productUpper;
    private int productLower;
    private float pcmValue;
    private boolean pcmIrqEnabled;
    private boolean pcmReadMode;
    private boolean pcmIrq;
    private boolean pulseCycle;

    public MMC5Audio(final TVSystem tvSystem) {
        this.mmc5 = null;
        setTVSystem(tvSystem);
    }

    public MMC5Audio(final MMC5 mmc5) {
        this.mmc5 = mmc5;
        setTVSystem(mmc5.getTVSystem());
    }

    public static void setVolume(final int volume) {
        final double pulsePercent = 1.0 / 3.0;
        final double pulseRange = MIX_RANGE * pulsePercent
                / (95.52 / (8128.0 / (pulseTable.length - 1) + 100.0));
        for (int i = pulseTable.length - 1; i >= 0; i--) {
            pulseTable[i] = toFloat(volume * 0.9552 / (8128.0 / i + 100.0),
                    pulseRange);
        }

        final double pcmPercent = 1.0 - pulsePercent;
        final double pcmRange = MIX_RANGE * pcmPercent;
        for (int i = pcmTable.length - 1; i >= 0; i--) {
            pcmTable[i] = toFloat(volume * i / (100.0 * (pcmTable.length - 1)),
                    pcmRange);
        }
    }

    private static float toFloat(double x, final double range) {
        if (x < 0) {
            x = 0;
        } else if (x > 1) {
            x = 1;
        }
        return ((float) (range * x));
    }

    public void setTVSystem(final TVSystem tvSystem) {
        audioUpdateThreshold = (float) ((tvSystem == NTSC
                ? NTSC_FREQUENCY : PAL_FREQUENCY) / AUDIO_UPDATE_FREQUENCY);
    }

    @Override
    public void reset() {
        audioUpdateTicks = 0f;
        multiplierA = 0;
        multiplierB = 0;
        productUpper = 0;
        productLower = 0;
        pcmValue = 0;
        pcmIrqEnabled = false;
        pcmReadMode = false;
        pcmIrq = false;
        pulseCycle = false;

        pulse1.reset();
        pulse2.reset();
    }

    public void updatePcmValue(int address, int value) {
        if (pcmReadMode && value != 0 && address >= 0x8000 && address < 0xC000) {
            pcmValue = pcmTable[value];
        }
    }

    @Override
    public int readRegister(int address) {
        switch (address) {
            case 0x5010:
                return readPcmMode();
            case 0x5015:
                return readAudioStatus();
            case 0x5205:
                return productLower;
            case 0x5206:
                return productUpper;
            default:
                return -1;
        }
    }

    @Override
    public boolean writeRegister(int address, int value) {
        switch (address) {
            case 0x5000:
                pulse1.writeEnvelope(value);
                return true;
            case 0x5002:
                pulse1.writeTimerReloadLow(value);
                return true;
            case 0x5003:
                pulse1.writeTimerReloadHigh(value);
                return true;
            case 0x5004:
                pulse2.writeEnvelope(value);
                return true;
            case 0x5006:
                pulse2.writeTimerReloadLow(value);
                return true;
            case 0x5007:
                pulse2.writeTimerReloadHigh(value);
                return true;
            case 0x5010:
                writePcmMode(value);
                return true;
            case 0x5011:
                writeRawPcm(value);
                return true;
            case 0x5015:
                writeAudioStatus(value);
                return true;
            case 0x5205:
                writeMultiplierA(value);
                return true;
            case 0x5206:
                writeMultiplierB(value);
                return true;
            default:
                return false;
        }
    }

    private void writeMultiplierA(final int value) {
        multiplierA = value;
        updateProduct();
    }

    private void writeMultiplierB(final int value) {
        multiplierB = value;
        updateProduct();
    }

    private void writePcmMode(final int value) {
        pcmReadMode = getBitBool(value, 0);
        pcmIrqEnabled = getBitBool(value, 7);
        if (mmc5 != null) {
            mmc5.updateIrq();
        }
    }

    private int readPcmMode() {
        int value = pcmIrq && pcmIrqEnabled ? 0x80 : 0x00;
        pcmIrq = false;
        if (mmc5 != null) {
            mmc5.updateIrq();
        }
        return value;
    }

    private void writeRawPcm(final int value) {
        if (!pcmReadMode && value > 0) {
            pcmValue = pcmTable[value];
        }
        pcmIrq = value == 0;
        if (mmc5 != null) {
            mmc5.updateIrq();
        }
    }

    private void writeAudioStatus(final int value) {
        pulse1.setEnabled(getBitBool(value, 0));
        pulse2.setEnabled(getBitBool(value, 1));
    }

    private int readAudioStatus() {
        return (pulse2.isEnabled() ? 0x02 : 0x00)
                | (pulse1.isEnabled() ? 0x01 : 0x00);
    }

    private void updateProduct() {
        int product = multiplierA * multiplierB;
        productUpper = (product >> 8) & 0xFF;
        productLower = product & 0xFF;
    }

    @Override
    public void update() {
        if (++audioUpdateTicks >= audioUpdateThreshold) {
            audioUpdateTicks -= audioUpdateThreshold;
            pulse1.updateEnvelopeGeneratorAndLengthCounter();
            pulse2.updateEnvelopeGeneratorAndLengthCounter();
        }
        if (pulseCycle) {
            pulse1.update();
            pulse2.update();
        }
        pulseCycle = !pulseCycle;
    }

    public boolean isIrq() {
        return pcmIrq && pcmIrqEnabled;
    }

    @Override
    public int getAudioMixerScale() {
        return 0xFFFF - MIX_RANGE;
    }

    @Override
    public float getAudioSample() {
        return pulseTable[pulse1.getValue() + pulse2.getValue()] + pcmValue;
    }
}
