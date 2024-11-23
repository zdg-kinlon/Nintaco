package nintaco.apu;

import java.io.Serializable;

import static nintaco.util.BitUtil.getBitBool;

public class SweepGenerator implements Serializable {

    private static final long serialVersionUID = 0;

    private int divider;
    private boolean reload;
    private boolean enabled;
    private int dividerPeriod;
    private boolean negate;
    private int shiftCount;
    private PulseGenerator pulseGenerator;

    public PulseGenerator getPulseGenerator() {
        return pulseGenerator;
    }

    public void setPulseGenerator(PulseGenerator pulseGenerator) {
        this.pulseGenerator = pulseGenerator;
    }

    public void write(int value) {
        enabled = getBitBool(value, 7);
        dividerPeriod = (value >> 4) & 7;
        negate = getBitBool(value, 3);
        shiftCount = value & 7;
    }

    public void update() {
        if (reload) {
            final int oldDivider = divider;
            divider = dividerPeriod;
            if (enabled && oldDivider == 0) {
                pulseGenerator.adjustPulsePeriod();
            }
            reload = false;
        } else if (divider > 0) {
            divider--;
        } else if (enabled) {
            divider = dividerPeriod;
            pulseGenerator.adjustPulsePeriod();
        }
    }

    public boolean isNegate() {
        return negate;
    }

    public int getShiftCount() {
        return shiftCount;
    }
}
