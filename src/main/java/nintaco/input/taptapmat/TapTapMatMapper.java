package nintaco.input.taptapmat;

import nintaco.input.DeviceMapper;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.TapTapMat;
import static nintaco.util.BitUtil.getBitBool;

public class TapTapMatMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int buttons;
    private int output = 0x1E;

    @Override
    public int getInputDevice() {
        return TapTapMat;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = ~buttons;
    }

    @Override
    public void writePort(final int value) {
        final int row = ~value & 0x07;
        output = 0;
        if (getBitBool(row, 2)) {
            output |= (buttons >> 23) & 0x1E;
        }
        if (getBitBool(row, 1)) {
            output |= (buttons >> 19) & 0x1E;
        }
        if (getBitBool(row, 0)) {
            output |= (buttons >> 15) & 0x1E;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        return (portIndex == 1) ? output : 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }

    @Override
    public void render(final int[] screen) {
        final int x = 179;
        final int y = 206;
        InputIcons.TapTapMat.render(screen, x, y);
        if (!getBitBool(buttons, 24)) {
            InputIcons.GamepadAB.render(screen, x + 2, y + 2);
        }
        if (!getBitBool(buttons, 25)) {
            InputIcons.GamepadAB.render(screen, x + 8, y + 2);
        }
        if (!getBitBool(buttons, 26)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 2);
        }
        if (!getBitBool(buttons, 27)) {
            InputIcons.GamepadAB.render(screen, x + 20, y + 2);
        }
        if (!getBitBool(buttons, 20)) {
            InputIcons.GamepadAB.render(screen, x + 2, y + 8);
        }
        if (!getBitBool(buttons, 21)) {
            InputIcons.GamepadAB.render(screen, x + 8, y + 8);
        }
        if (!getBitBool(buttons, 22)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 8);
        }
        if (!getBitBool(buttons, 23)) {
            InputIcons.GamepadAB.render(screen, x + 20, y + 8);
        }
        if (!getBitBool(buttons, 16)) {
            InputIcons.GamepadAB.render(screen, x + 2, y + 14);
        }
        if (!getBitBool(buttons, 17)) {
            InputIcons.GamepadAB.render(screen, x + 8, y + 14);
        }
        if (!getBitBool(buttons, 18)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 14);
        }
        if (!getBitBool(buttons, 19)) {
            InputIcons.GamepadAB.render(screen, x + 20, y + 14);
        }
    }
}
