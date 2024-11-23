package nintaco.apu;

import java.io.Serializable;

import static nintaco.util.BitUtil.getBitBool;

public class TriangleGenerator implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[] waveform = {
            15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    };

    private boolean control;
    private boolean linearCounterReload;
    private int linearCounterReloadValue;
    private int timer;
    private int timerReloadValue;
    private int lengthCounter;
    private int waveformIndex;
    private int linearCounter;
    private boolean enabled;

    public void writeLinearCounter(int value) {
        control = getBitBool(value, 7);
        linearCounterReloadValue = 0x7F & value;
    }

    public void writeTimerReloadLow(int value) {
        timerReloadValue = (timerReloadValue & 0xFF00) | value;
    }

    public void writeTimerReloadHigh(int value) {
        if (enabled) {
            lengthCounter = APU.lengths[value >> 3];
        }
        timerReloadValue = ((value & 7) << 8) | (timerReloadValue & 0x00FF);
        linearCounterReload = true;
    }

    public void updateLengthCounter() {
        if (!control && lengthCounter > 0) {
            lengthCounter--;
        }
    }

    public void updateLinearCounter() {
        if (linearCounterReload) {
            linearCounter = linearCounterReloadValue;
        } else if (linearCounter > 0) {
            linearCounter--;
        }
        if (!control) {
            linearCounterReload = false;
        }
    }

    public void update() {
        if (timer == 0) {
            timer = timerReloadValue;
            if (linearCounter != 0 && lengthCounter != 0 && timerReloadValue >= 2) {
                waveformIndex = (waveformIndex - 1) & 0x1F;
            }
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
            linearCounter = 0;
        }
    }

    public int getLengthCounter() {
        return lengthCounter;
    }

    public int getValue() {
        return waveform[waveformIndex];
    }
}
