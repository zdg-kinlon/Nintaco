package nintaco.mappers.konami.vrc6;

import static nintaco.util.BitUtil.getBitBool;

public class VrcPulseGenerator extends VrcChannel {

    private static final long serialVersionUID = 0;

    private int volume;
    private int dutyReset;
    private boolean ignoreDuty;
    private int duty = 15;

    public void reset() {
        volume = 0;
        dutyReset = 0;
        ignoreDuty = false;
        duty = 15;
    }

    public void writeControl(int value) {
        volume = value & 0x0F;
        dutyReset = (value >> 4) & 0x07;
        ignoreDuty = getBitBool(value, 7);

        outputLevel = ignoreDuty ? volume : (duty <= dutyReset ? volume : 0);
    }

    public void update() {
        if (runOscillator) {
            if (frequency == 0) {
                frequency = (frequencyReload >> frequencyShift);
                if (enabled) {
                    if (duty == 0) {
                        duty = 15;
                    } else {
                        duty--;
                    }
                }
                if (!ignoreDuty) {
                    outputLevel = duty <= dutyReset ? volume : 0;
                }
            } else {
                frequency--;
            }
        }
    }

    @Override
    public void writeFrequencyHigh(int value) {
        super.writeFrequencyHigh(value);
        if (!enabled) {
            duty = 15;
        }
    }
}
