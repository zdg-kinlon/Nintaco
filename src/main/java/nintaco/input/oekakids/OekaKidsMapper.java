package nintaco.input.oekakids;

import nintaco.input.DeviceMapper;

import java.io.Serializable;

import static nintaco.input.InputDevices.OekaKids;
import static nintaco.util.BitUtil.getBitBool;

public class OekaKidsMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[] XS = new int[256];
    private static final int[] YS = new int[256];

    static {
        for (int i = XS.length - 1; i >= 0; i--) {
            XS[i] = (((i * 240) >> 8) + 8) << 10;
            YS[i] = (((i << 8) / 240) - 14) << 2;
        }
    }

    private int buttons;
    private int shiftRegister;
    private boolean readMode;
    private boolean shift;

    @Override
    public int getInputDevice() {
        return OekaKids;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {
        final boolean nextReadMode = getBitBool(value, 0);
        final boolean nextShift = getBitBool(value, 1);
        if (nextReadMode) {
            if (!shift && nextShift) {
                shiftRegister <<= 1;
            }
        } else {
            shiftRegister = ~(XS[(buttons >> 16) & 0xFF]
                    | YS[(buttons >> 24) & 0xFF]
                    | ((buttons >> 10) & 0x00000003));
        }
        readMode = nextReadMode;
        shift = nextShift;
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            if (readMode) {
                if (shift) {
                    return (shiftRegister >> 15) & 0x08;
                } else {
                    return 0x04;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }
}