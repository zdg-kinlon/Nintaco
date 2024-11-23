package nintaco.input.uforce;

import nintaco.input.DeviceMapper;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.UForce;
import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.MathUtil.clamp;

public class UForceMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int RELEASED = 0;
    private static final int PRESSED = 0x1E;

    private int shiftRegister;
    private int buttons;
    private int byteIndex;
    private boolean strobe;

    private int topSensor;
    private boolean topSensorPressed;

    private int bottomSensor;
    private boolean bottomSensorPressed;

    private boolean enabled1;
    private boolean enabled3A;

    @Override
    public int getInputDevice() {
        return UForce;
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            switch (byteIndex) {
                case 0:
                    shiftRegister = (buttons >> 9) & 0x03;
                    break;
                case 1:
                    if (enabled1) {
                        setShiftRegisterFromButtons(6);
                    } else {
                        setShiftRegister(bottomSensor);
                    }
                    break;
                case 2:
                    setShiftRegisterFromButtons(7);
                    break;
                case 3:
                    if (enabled3A) {
                        setShiftRegisterFromButtons(4);
                    } else {
                        setShiftRegister(bottomSensor);
                    }
                    break;
                case 4:
                    setShiftRegisterFromButtons(5);
                    break;
                case 5:
                    setShiftRegisterFromButtons(3);
                    break;
                case 6:
                    setShiftRegisterFromButtons(1);
                    break;
                case 7:
                    setShiftRegisterFromButtons(2);
                    break;
                case 8:
                    setShiftRegister(topSensor);
                    break;
            }
            if (++byteIndex > 8) {
                byteIndex = 0;
            }
        }
    }

    private void setShiftRegisterFromButtons(final int bit) {
        setShiftRegister(getBitBool(buttons, bit) ? PRESSED : RELEASED);
    }

    private void setShiftRegister(int value) {
        shiftRegister = (value == RELEASED) ? 0x02 : 0x00;
        value ^= 0x1F;
        shiftRegister |= (value << 3) | ((value & 1) << 2);
    }

    @Override
    public void update(final int buttons) {

        this.buttons = buttons;

        if (getBitBool(buttons, 0)) {
            topSensorPressed = true;
            topSensor = PRESSED;
        } else if (topSensorPressed) {
            topSensorPressed = false;
            topSensor = RELEASED;
        } else {
            topSensor = clamp(topSensor + (byte) (buttons >> 16), RELEASED, PRESSED);
        }

        if (getBitBool(buttons, 8)) {
            bottomSensorPressed = true;
            enabled3A = false;
            bottomSensor = PRESSED;
        } else if (bottomSensorPressed) {
            bottomSensorPressed = false;
            enabled3A = false;
            bottomSensor = RELEASED;
        } else {
            final int deltaY = (byte) (buttons >> 24);
            bottomSensor = clamp(bottomSensor - deltaY, RELEASED, PRESSED);
            if (deltaY != 0) {
                enabled3A = enabled1 = false;
            }
        }

        if (getBitBool(buttons, 4)) {
            enabled3A = true;
            bottomSensor = RELEASED;
        }
        if (getBitBool(buttons, 6)) {
            enabled1 = true;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 0) {
            final int value = getBit(shiftRegister, 7);
            if (!strobe) {
                shiftRegister <<= 1;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 0) ? (shiftRegister & 1) : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 8;
        final int y = 170;
        InputIcons.UForce.render(screen, x, y);
        if (getBitBool(buttons, 9)) {
            InputIcons.FamilyBasicKeyboardKey.render(screen, x + 7, y + 50);
        }
        if (getBitBool(buttons, 10)) {
            InputIcons.FamilyBasicKeyboardKey.render(screen, x + 4, y + 48);
        }
        if (topSensor != RELEASED) {
            InputIcons.GamepadAB.render(screen, x + 10, y + 2);
        }
        if (getBitBool(buttons, 1)) {
            InputIcons.GamepadAB.render(screen, x + 6, y + 9);
        }
        if (getBitBool(buttons, 2)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 9);
        }
        if (getBitBool(buttons, 3)) {
            InputIcons.GamepadAB.render(screen, x + 6, y + 18);
        }
        if (enabled3A ? getBitBool(buttons, 4) : (bottomSensor != RELEASED)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 18);
        }
        if (getBitBool(buttons, 5)) {
            InputIcons.GamepadAB.render(screen, x + 6, y + 32);
        }
        if (enabled1 ? getBitBool(buttons, 6) : (bottomSensor != RELEASED)) {
            InputIcons.GamepadAB.render(screen, x + 6, y + 41);
        }
        if (getBitBool(buttons, 7)) {
            InputIcons.GamepadAB.render(screen, x + 14, y + 41);
        }
        if (bottomSensor != RELEASED) {
            InputIcons.GamepadAB.render(screen, x + 10, y + 48);
        }
    }
}