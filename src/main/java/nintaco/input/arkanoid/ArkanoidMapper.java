package nintaco.input.arkanoid;

import nintaco.input.DeviceMapper;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.Arkanoid;
import static nintaco.input.Ports.ExpansionPort;
import static nintaco.util.MathUtil.clamp;

public class ArkanoidMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[] KNOB_VALUES;

    static {
        KNOB_VALUES = new int[144];
        for (int i = KNOB_VALUES.length - 1; i >= 0; i--) {
            KNOB_VALUES[i] = ~(i * (0xA0 - 14) / 143 + 0x54 + 14);
        }
    }

    private final int portIndex;
    private final int shift;
    private final boolean famicom;

    private int buttons;
    private int shiftRegister;
    private int fire;
    private boolean strobe;

    public ArkanoidMapper(final int portIndex) {
        this.portIndex = portIndex;
        shift = portIndex == 0 ? 0 : 8;
        famicom = portIndex == ExpansionPort;
    }

    @Override
    public int getInputDevice() {
        return Arkanoid;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            shiftRegister = KNOB_VALUES[clamp(((buttons >> 16) & 0xFF) - 32, 0, 143)];
            fire = (buttons >> shift) & 0x04;
            if (famicom) {
                fire >>= 1;
            } else {
                fire <<= 1;
            }
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            if (famicom) {
                if (portIndex == 0) {
                    return fire;
                } else {
                    if (!strobe) {
                        shiftRegister <<= 1;
                    }
                    return (shiftRegister >> 7) & 0x02;
                }
            } else {
                if (!strobe) {
                    shiftRegister <<= 1;
                }
                return ((shiftRegister >> 4) & 0x10) | fire;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            if (famicom) {
                if (portIndex == 0) {
                    return fire;
                } else {
                    return (shiftRegister >> 7) & 0x02;
                }
            } else {
                return ((shiftRegister >> 4) & 0x10) | fire;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void render(final int[] screen) {
        final int x = famicom ? 172 : 19 + 59 * portIndex;
        final int y = 206;
        InputIcons.Arkanoid.render(screen, x, y);
        if (fire != 0) {
            InputIcons.GamepadAB.render(screen, x + 31, y + 7);
        }
    }
}