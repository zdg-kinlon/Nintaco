package cn.kinlon.emu.input.excitingboxing;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.ExcitingBoxing;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class ExcitingBoxingMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int state = 0x1E;
    private int buttons;

    @Override
    public int getInputDevice() {
        return ExcitingBoxing;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {

        int bits = 0;
        if (getBitBool(value, 1)) {
            if (getBitBool(buttons, 20)) {
                bits |= 0x10;
            }
            if (getBitBool(buttons, 19)) {
                bits |= 0x08;
            }
            if (getBitBool(buttons, 21)) {
                bits |= 0x04;
            }
            if (getBitBool(buttons, 18)) {
                bits |= 0x02;
            }
        } else {
            if (getBitBool(buttons, 17)) {
                bits |= 0x10;
            }
            if (getBitBool(buttons, 23)) {
                bits |= 0x08;
            }
            if (getBitBool(buttons, 22)) {
                bits |= 0x04;
            }
            if (getBitBool(buttons, 16)) {
                bits |= 0x02;
            }
        }
        state = ~bits & 0x1E;
    }

    @Override
    public int readPort(final int portIndex) {
        return portIndex == 1 ? state : 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }

    @Override
    public void render(final int[] screen) {
        final int x = 176;
        final int y = 206;
        InputIcons.ExcitingBoxing.render(screen, x, y);
        if (getBitBool(buttons, 16)) {
            InputIcons.GamepadAB.render(screen, x + 8, y + 2);
        }
        if (getBitBool(buttons, 17)) {
            InputIcons.GamepadAB.render(screen, x + 20, y + 2);
        }
        if (getBitBool(buttons, 18)) {
            InputIcons.GamepadAB.render(screen, x + 8, y + 8);
        }
        if (getBitBool(buttons, 19)) {
            InputIcons.GamepadAB.render(screen, x + 20, y + 8);
        }
        if (getBitBool(buttons, 20)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 8);
        }
        if (getBitBool(buttons, 21)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 14);
        }
        if (getBitBool(buttons, 22)) {
            InputIcons.ExcitingBoxingLeft.render(screen, x + 2, y + 7);
        }
        if (getBitBool(buttons, 23)) {
            InputIcons.ExcitingBoxingRight.render(screen, x + 27, y + 7);
        }
    }
}
