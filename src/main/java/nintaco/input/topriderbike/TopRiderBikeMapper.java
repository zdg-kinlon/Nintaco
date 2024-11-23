package nintaco.input.topriderbike;

import nintaco.input.DeviceMapper;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.TopRiderBike;
import static nintaco.util.BitUtil.getBitBool;

public class TopRiderBikeMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int MAX_STEER = 20;
    private static final int MAX_BRAKING = 20;
    private static final int MAX_ACCELERATION = 20;

    private static final int DEADZONE_MAX = 16;
    private static final int DEADZONE_MID = 10;
    private static final int DEADZONE_MIN = 4;

    private int buttons;
    private int bits;
    private int state0;
    private int state1;
    private int shiftRegister0;
    private int shiftRegister1;
    private int acceleration;
    private int braking;
    private int steer;
    private boolean strobe;

    @Override
    public int getInputDevice() {
        return TopRiderBike;
    }

    @Override
    public void update(final int buttons) {

        this.buttons = buttons;

        final boolean steerRight = getBitBool(buttons, 23);
        final boolean steerLeft = getBitBool(buttons, 22);
        final boolean wheelie = getBitBool(buttons, 21);
        final boolean shiftGear = getBitBool(buttons, 20);
        final boolean start = getBitBool(buttons, 19);
        final boolean select = getBitBool(buttons, 18);
        final boolean brake = getBitBool(buttons, 17);
        final boolean accelerate = getBitBool(buttons, 16);

        if (steerLeft == steerRight) {
            if (steer > 0) {
                steer--;
            } else if (steer < 0) {
                steer++;
            }
        } else if (steerLeft) {
            if (steer > -MAX_STEER) {
                steer--;
            }
        } else if (steerRight) {
            if (steer < MAX_STEER) {
                steer++;
            }
        }

        if (brake) {
            if (braking < MAX_BRAKING) {
                braking++;
            }
        } else if (braking > 0) {
            braking--;
        }

        if (accelerate) {
            if (acceleration < MAX_ACCELERATION) {
                acceleration++;
            }
        } else if (acceleration > 0) {
            acceleration--;
        }

        bits &= 0xC0;

        if (shiftGear) {
            if ((bits & 0x40) == 0) {
                bits = (bits ^ 0x80) | 0x40;
            }
        } else {
            bits &= 0xBF;
        }

        if (wheelie) {
            bits |= 0x01;
        }
        if (select) {
            bits |= 0x20;
        }
        if (start) {
            bits |= 0x10;
        }

        int data = 0;
        if (steer > 0) {
            if (steer > DEADZONE_MAX) {
                data = 0x0A0;
            } else if (steer > DEADZONE_MID) {
                data = 0x020;
            } else if (steer > DEADZONE_MIN) {
                data = 0x080;
            }
        } else if (steer < -DEADZONE_MAX) {
            data = 0x050;
        } else if (steer < -DEADZONE_MID) {
            data = 0x040;
        } else if (steer < -DEADZONE_MIN) {
            data = 0x100;
        }
        state0 = ((bits & 0x01) << 11) | ((bits & 0x80) << 3) | data;

        data = 0;
        if (acceleration > 8 || braking < 8) {
            if (acceleration > DEADZONE_MAX) {
                data = 0x008;
            } else if (acceleration > DEADZONE_MID) {
                data = 0x080;
            } else if (acceleration > DEADZONE_MIN) {
                data = 0x100;
            }
        } else {
            state0 |= 0x200;
            if (braking > DEADZONE_MAX) {
                data = 0x010;
            } else if (braking > DEADZONE_MID) {
                data = 0x020;
            } else if (braking > DEADZONE_MIN) {
                data = 0x040;
            }
        }
        state1 = ((bits & 0x30) << 5) | data;
    }

    @Override
    public void writePort(int value) {
        final boolean priorStrobe = strobe;
        strobe = getBitBool(value, 0);
        if (priorStrobe && !strobe) {
            shiftRegister0 = state0;
            shiftRegister1 = state1;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            final int value = (shiftRegister0 & 0x10) | (shiftRegister1 & 0x08);
            shiftRegister0 >>= 1;
            shiftRegister1 >>= 1;
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 1) ? (shiftRegister0 & 0x10) | (shiftRegister1 & 0x08)
                : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 164;
        final int y = 208;
        InputIcons.TopRider.render(screen, x, y);
        if (getBitBool(buttons, 23)) {
            InputIcons.ExcitingBoxingRight.render(screen, x + 29, y + 3);
        }
        if (getBitBool(buttons, 22)) {
            InputIcons.ExcitingBoxingLeft.render(screen, x + 22, y + 3);
        }
        if (getBitBool(buttons, 21)) {
            InputIcons.TopRiderHandle.render(screen, x + 1, y + 8);
        }
        if (getBitBool(buttons, 20)) {
            InputIcons.TopRiderShift.render(screen, x + 13, y + 11);
        }
        if (getBitBool(buttons, 19)) {
            InputIcons.GamepadStart.render(screen, x + 28, y + 12);
        }
        if (getBitBool(buttons, 18)) {
            InputIcons.GamepadStart.render(screen, x + 22, y + 12);
        }
        if (getBitBool(buttons, 17)) {
            InputIcons.TopRiderBrake.render(screen, x + 41, y + 1);
        }
        if (getBitBool(buttons, 16)) {
            InputIcons.TopRiderHandle.render(screen, x + 43, y + 8);
        }
    }
}