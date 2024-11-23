package nintaco.input.powerpad;

import nintaco.input.DeviceMapper;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.PowerPad;
import static nintaco.util.BitUtil.getBitBool;

public class PowerPadMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private final int portIndex;

    private int buttons;
    private int shiftRegister;

    public PowerPadMapper(final int portIndex) {
        this.portIndex = portIndex;
    }

    @Override
    public int getInputDevice() {
        return PowerPad;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {
        if (getBitBool(value, 0)) {
            shiftRegister = buttons >> 11;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            shiftRegister >>= 2;
            return shiftRegister & 0x18;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return this.portIndex == portIndex ? (shiftRegister & 0x18) : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = (portIndex << 6) + 20;
        final int y = 205;
        InputIcons.PowerPad.render(screen, x, y);
        if ((buttons & 0x00040000) != 0) { //  1
            InputIcons.GamepadAB.render(screen, x + 2, y + 2);
        }
        if ((buttons & 0x00010000) != 0) { //  2
            InputIcons.GamepadAB.render(screen, x + 7, y + 2);
        }
        if ((buttons & 0x00080000) != 0) { //  3
            InputIcons.GamepadAB.render(screen, x + 13, y + 2);
        }
        if ((buttons & 0x00020000) != 0) { //  4
            InputIcons.GamepadAB.render(screen, x + 18, y + 2);
        }
        if ((buttons & 0x00100000) != 0) { //  5
            InputIcons.GamepadAB.render(screen, x + 2, y + 8);
        }
        if ((buttons & 0x01000000) != 0) { //  6
            InputIcons.GamepadAB.render(screen, x + 7, y + 8);
        }
        if ((buttons & 0x40000000) != 0) { //  7
            InputIcons.GamepadAB.render(screen, x + 13, y + 8);
        }
        if ((buttons & 0x00800000) != 0) { //  8
            InputIcons.GamepadAB.render(screen, x + 18, y + 8);
        }
        if ((buttons & 0x00400000) != 0) { //  9
            InputIcons.GamepadAB.render(screen, x + 2, y + 14);
        }
        if ((buttons & 0x04000000) != 0) { // 10
            InputIcons.GamepadAB.render(screen, x + 7, y + 14);
        }
        if ((buttons & 0x10000000) != 0) { // 11
            InputIcons.GamepadAB.render(screen, x + 13, y + 14);
        }
        if ((buttons & 0x00200000) != 0) { // 12
            InputIcons.GamepadAB.render(screen, x + 18, y + 14);
        }
    }
}