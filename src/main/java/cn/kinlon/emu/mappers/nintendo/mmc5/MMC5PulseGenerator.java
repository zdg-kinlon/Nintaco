package cn.kinlon.emu.mappers.nintendo.mmc5;

import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.EnvelopeGenerator;

import java.io.Serializable;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class MMC5PulseGenerator implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][] waveforms = {
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1},
    };
    private final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    private int duty;
    private int timer;
    private int timerReload;
    private int waveformIndex;
    private int lengthCounter;
    private boolean lengthCounterEnabled;
    private boolean enabled;

    public void reset() {
        duty = 0;
        timer = 0;
        timerReload = 0;
        waveformIndex = 0;
        lengthCounter = 0;
        lengthCounterEnabled = false;
        enabled = false;

        envelopeGenerator.reset();
    }

    public void writeEnvelope(int value) {
        envelopeGenerator.write(value);
        lengthCounterEnabled = !getBitBool(value, 5);
        duty = (value >> 6) & 3;
    }

    public void writeTimerReloadLow(int value) {
        timerReload = (timerReload & 0x0700) | value;
    }

    public void writeTimerReloadHigh(int value) {
        if (enabled) {
            lengthCounter = APU.lengths[value >> 3];
        }
        timerReload = (timerReload & 0x00FF) | ((value & 7) << 8);
        waveformIndex = 0;
        envelopeGenerator.setStart(true);
    }

    public void updateEnvelopeGeneratorAndLengthCounter() {
        envelopeGenerator.update();
        if (lengthCounterEnabled && lengthCounter > 0) {
            lengthCounter--;
        }
    }

    public void update() {
        if (timer == 0) {
            timer = timerReload;
            waveformIndex = (waveformIndex - 1) & 7;
        } else {
            timer--;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            lengthCounter = 0;
        }
    }

    public int getValue() {
        if (lengthCounter == 0) {
            return 0;
        } else {
            return waveforms[duty][waveformIndex] == 0 ? 0
                    : envelopeGenerator.getVolume();
        }
    }
}