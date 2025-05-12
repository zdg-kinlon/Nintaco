package cn.kinlon.emu.mappers.konami.vrc6;

import java.io.Serializable;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public abstract class VrcChannel implements Serializable {

    private static final long serialVersionUID = 0;

    protected boolean runOscillator;
    protected int frequencyShift;
    protected int frequencyReload;
    protected int frequency;
    protected int outputLevel;
    protected boolean enabled;

    public void writeFrequencyControl(int value) {
        runOscillator = !getBitBool(value, 0);
        switch ((value >> 1) & 3) {
            case 0:
                frequencyShift = 0;
                break;
            case 1:
                frequencyShift = 4;
                break;
            case 2:
            case 3:
                frequencyShift = 8;
                break;
        }
    }

    public void writeFrequencyLow(int value) {
        frequencyReload = (0x0F00 & frequencyReload) | value;
    }

    public void writeFrequencyHigh(int value) {
        frequencyReload = ((value & 0x0F) << 8) | (0x00FF & frequencyReload);
        enabled = getBitBool(value, 7);
    }

    public int getValue() {
        return enabled ? outputLevel : 0;
    }
}
