package cn.kinlon.emu.apu;

import java.io.Serializable;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class PulseGenerator implements Serializable {

    private static final long serialVersionUID = 0;

    private static final boolean[][] waveforms = {
            {false, true, false, false, false, false, false, false},
            {false, true, true, false, false, false, false, false},
            {false, true, true, true, true, false, false, false},
            {true, false, false, true, true, true, true, true},
    };

    private final boolean pulseGenerator1;
    private final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    private final SweepGenerator sweepGenerator = new SweepGenerator();
    private int duty;
    private int timer;
    private int targetPeriod;
    private int currentPeriod;
    private int waveformIndex;
    private int lengthCounter;
    private boolean lengthCounterEnabled;
    private boolean enabled;

    public PulseGenerator(final boolean pulseGenerator1) {
        this.pulseGenerator1 = pulseGenerator1;
        sweepGenerator.setPulseGenerator(this);
    }

    private void updateTargetPeriod() {
        final int delta = currentPeriod >> sweepGenerator.getShiftCount();
        if (sweepGenerator.isNegate()) {
            if (pulseGenerator1) {
                targetPeriod = currentPeriod + ~delta;
            } else {
                targetPeriod = currentPeriod - delta;
            }
        } else {
            targetPeriod = currentPeriod + delta;
        }
    }

    public void adjustPulsePeriod() {
        updateTargetPeriod();
        if (sweepGenerator.getShiftCount() != 0 && targetPeriod <= 0x07FF
                && currentPeriod >= 8) {
            currentPeriod = targetPeriod & 0x07FF;
        }
    }

    public void writeEnvelope(final int value) {
        envelopeGenerator.write(value);
        lengthCounterEnabled = !getBitBool(value, 5);
        duty = (value >> 6) & 3;
    }

    public void writeSweep(final int value) {
        sweepGenerator.write(value);
    }

    public void writeTimerReloadLow(final int value) {
        currentPeriod = (currentPeriod & 0x0700) | value;
        updateTargetPeriod();
    }

    public void writeTimerReloadHigh(final int value) {
        if (enabled) {
            lengthCounter = APU.lengths[value >> 3];
        }
        currentPeriod = (currentPeriod & 0x00FF) | ((value & 7) << 8);
        waveformIndex = 0;
        envelopeGenerator.setStart(true);
        updateTargetPeriod();
    }

    public void updateEnvelopeGenerator() {
        envelopeGenerator.update();
    }

    public void updateLengthCounterAndSweepGenerator() {
        if (lengthCounterEnabled && lengthCounter > 0) {
            lengthCounter--;
        }
        sweepGenerator.update();
    }

    public void update() {
        if (timer == 0) {
            timer = currentPeriod;
            waveformIndex = (waveformIndex - 1) & 7;
        } else {
            timer--;
        }
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
        if (targetPeriod > 0x07FF || currentPeriod < 8 || lengthCounter == 0) {
            return 0;
        } else {
            return waveforms[duty][waveformIndex] ? envelopeGenerator.getVolume() : 0;
        }
    }
}