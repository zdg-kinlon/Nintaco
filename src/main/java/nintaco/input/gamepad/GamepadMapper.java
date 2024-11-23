package nintaco.input.gamepad;

import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.Gamepad1;
import static nintaco.input.gamepad.GamepadDescriptor.*;
import static nintaco.util.BitUtil.getBitBool;

public class GamepadMapper extends LagDeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private final int shift;
    private final int portIndex;

    private int shiftRegister;
    private boolean strobe;

    public GamepadMapper(final int portIndex) {
        this.shift = portIndex << 3;
        this.portIndex = portIndex;
    }

    public static void render(final int[] screen, final int portIndex,
                              final int buttons) {
        final int x = 19 + 59 * portIndex;
        final int y = 208;
        InputIcons.Gamepad.render(screen, x, y);
        if (getBitBool(buttons, A)) {
            InputIcons.GamepadAB.render(screen, x + 31, y + 9);
        }
        if (getBitBool(buttons, B)) {
            InputIcons.GamepadAB.render(screen, x + 26, y + 9);
        }
        if (getBitBool(buttons, Select)) {
            InputIcons.GamepadStart.render(screen, x + 14, y + 10);
        }
        if (getBitBool(buttons, Start)) {
            InputIcons.GamepadStart.render(screen, x + 19, y + 10);
        }
        if (getBitBool(buttons, Up)) {
            InputIcons.GamepadDPad.render(screen, x + 5, y + 5);
        }
        if (getBitBool(buttons, Down)) {
            InputIcons.GamepadDPad.render(screen, x + 5, y + 11);
        }
        if (getBitBool(buttons, Left)) {
            InputIcons.GamepadDPad.render(screen, x + 2, y + 8);
        }
        if (getBitBool(buttons, Right)) {
            InputIcons.GamepadDPad.render(screen, x + 8, y + 8);
        }
    }

    public int getPortIndex() {
        return portIndex;
    }

    @Override
    public int getInputDevice() {
        return Gamepad1;
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            updateButtons();
            shiftRegister = 0xFFFFFF00 | ((buttons >> shift) & 0xFF);
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            final int value = shiftRegister & 1;
            if (!strobe) {
                shiftRegister >>= 1;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return this.portIndex == portIndex ? (shiftRegister & 1) : 0;
    }

    @Override
    public void render(final int[] screen) {
        render(screen, portIndex, (buttons >> shift) & 0xFF);
    }
}