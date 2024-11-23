package nintaco.apu;

import java.io.Serializable;

import static nintaco.util.BitUtil.getBitBool;

public class NoiseGenerator implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][] PERIODS = {
            {2, 4, 8, 16, 32, 48, 64, 80, 101, 127, 190, 254, 381, 508, 1017, 2034},
            {2, 4, 7, 15, 30, 44, 59, 74, 94, 118, 177, 236, 354, 472, 945, 1889},
    };
    private final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    private int shiftRegister = 1;
    private int lengthCounter;
    private int timerPeriod;
    private int timer;
    private boolean lengthCounterEnabled;
    private boolean mode;
    private boolean enabled;
    private int[] periods;

    public void setPAL(final boolean pal) {
        periods = PERIODS[pal ? 1 : 0];
    }

    public void writeEnvelope(final int value) {
        envelopeGenerator.write(value);
        lengthCounterEnabled = !getBitBool(value, 5);
    }

    public void writeModeAndPeriod(final int value) {
        mode = getBitBool(value, 7);
        timerPeriod = periods[value & 0x0F];
    }

    public void writeLengthCounter(final int value) {
        if (enabled) {
            lengthCounter = APU.lengths[value >> 3];
        }
        envelopeGenerator.setStart(true);
    }

    public void updateEnvelopeGenerator() {
        envelopeGenerator.update();
    }

    public void updateLengthCounter() {
        if (lengthCounterEnabled && lengthCounter > 0) {
            lengthCounter--;
        }
    }

    public void update() {
        if (timer == 0) {
            timer = timerPeriod;
            shiftRegister = (shiftRegister >> 1)
                    | (((shiftRegister & 1)
                    ^ ((shiftRegister >> (mode ? 6 : 1)) & 1)) << 14);
        } else {
            timer--;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            lengthCounter = 0;
        }
    }

    public int getLengthCounter() {
        return lengthCounter;
    }

    public int getValue() {
        if (lengthCounter == 0 || (shiftRegister & 1) == 1) {
            return 0;
        } else {
            return envelopeGenerator.getVolume();
        }
    }
}