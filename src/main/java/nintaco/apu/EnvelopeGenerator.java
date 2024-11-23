package nintaco.apu;

import java.io.Serializable;

import static nintaco.util.BitUtil.getBitBool;

public class EnvelopeGenerator implements Serializable {

    private static final long serialVersionUID = 0;

    private boolean loop;
    private boolean constantVolume;
    private int volumePeriod;
    private boolean start;
    private int divider;
    private int counter;

    public void reset() {
        loop = false;
        constantVolume = false;
        volumePeriod = 0;
        start = false;
        divider = 0;
        counter = 0;
    }

    public boolean getStart() {
        return start;
    }

    public void setStart(final boolean start) {
        this.start = start;
    }

    public void write(final int value) {
        loop = getBitBool(value, 5);
        constantVolume = getBitBool(value, 4);
        volumePeriod = value & 0x0F;
    }

    public void update() {
        if (start) {
            start = false;
            counter = 15;
            divider = volumePeriod;
        } else {
            if (--divider < 0) {
                divider = volumePeriod;
                if (counter == 0) {
                    if (loop) {
                        counter = 15;
                    }
                } else {
                    counter--;
                }
            }
        }
    }

    public int getVolume() {
        if (constantVolume) {
            return volumePeriod;
        } else {
            return counter;
        }
    }
}
