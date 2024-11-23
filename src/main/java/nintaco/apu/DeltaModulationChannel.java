package nintaco.apu;

import nintaco.CPU;

import java.io.Serializable;

import static nintaco.util.BitUtil.getBitBool;

public class DeltaModulationChannel implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][] RATES = {
            {214, 190, 170, 160, 143, 127, 113, 107, 95, 80, 71, 64, 53, 42, 36, 27},
            {199, 177, 158, 149, 138, 118, 105, 99, 88, 74, 66, 59, 49, 39, 33, 25},
    };

    private boolean irqEnabled;
    private boolean loop;
    private CPU cpu;
    private int timer;
    private int timerReload;
    private int outputLevel;
    private int sampleAddress;
    private int sampleLength;
    private int sampleBuffer;
    private int shiftRegister;
    private int bitsRemaining = 8;
    private boolean silence = true;
    private int currentAddress;
    private int bytesRemaining;
    private boolean sampleBufferFilled;
    private boolean requestedSample;
    private int[] rates;
    private float smoothDelta;
    private float smoothLevel;
    private int smoothSteps;

    public void setCPU(final CPU cpu) {
        this.cpu = cpu;
    }

    public void setPAL(final boolean pal) {
        rates = RATES[pal ? 1 : 0];
        timer = 1;
        timerReload = rates[0];
    }

    public int getBytesRemaining() {
        return bytesRemaining;
    }

    public void setEnabled(final boolean enabled) {
        if (enabled) {
            if (bytesRemaining == 0) {
                currentAddress = sampleAddress;
                bytesRemaining = sampleLength;
            }
        } else {
            bytesRemaining = 0;
        }
        cpu.setDmcIrq(false);
    }

    public void writeFlagsAndFrequency(final int value) {
        irqEnabled = getBitBool(value, 7);
        if (!irqEnabled) {
            cpu.setDmcIrq(false);
        }
        loop = getBitBool(value, 6);
        timerReload = rates[value & 0x0F];
    }

    public void writeDirectLoad(final int value) {
        smoothLevel = outputLevel;
        outputLevel = value & 0x7F;
        smoothLevel -= outputLevel;
        smoothSteps = 255;
        smoothDelta = -0.00390625f * smoothLevel;
        smoothLevel += smoothDelta;
    }

    public void writeSampleAddress(final int value) {
        sampleAddress = 0xC000 | (value << 6);
    }

    public void writeSampleLength(final int value) {
        sampleLength = (value << 4) | 1;
    }

    public void fillSampleBuffer(final int value) {
        sampleBuffer = value;
        sampleBufferFilled = true;
        if (++currentAddress > 0xFFFF) {
            currentAddress = 0x8000;
        }
        if (bytesRemaining == 0) {
            if (loop) {
                currentAddress = sampleAddress;
                bytesRemaining = sampleLength;
            } else if (irqEnabled) {
                cpu.setDmcIrq(true);
            }
        }
        requestedSample = false;
    }

    public void update() {

        if (smoothSteps > 0) {
            smoothSteps--;
            smoothLevel += smoothDelta;
        } else {
            smoothLevel = 0;
        }

        if (--timer == 0) {
            timer = timerReload;
            if (!silence) {
                if ((shiftRegister & 1) == 1) {
                    if (outputLevel <= 125) {
                        outputLevel += 2;
                    }
                } else {
                    if (outputLevel >= 2) {
                        outputLevel -= 2;
                    }
                }
                shiftRegister >>= 1;
            }
            if (--bitsRemaining == 0) {

                bitsRemaining = 8;

                if (sampleBufferFilled) {
                    silence = false;
                    sampleBufferFilled = false;
                    shiftRegister = sampleBuffer;
                } else {
                    silence = true;
                }
            }
        }
        if (!requestedSample && !sampleBufferFilled && bytesRemaining > 0) {
            requestedSample = true;
            cpu.dmcRead(currentAddress);
            bytesRemaining--;
        }
    }

    public int getOutputLevel() {
        return outputLevel;
    }

    public float getSmoothLevel() {
        return smoothLevel;
    }
}