package nintaco.input.horitrack;

import net.java.games.input.Component;
import nintaco.input.*;
import nintaco.input.gamepad.Autofire;

import static net.java.games.input.Component.Identifier.Button.Button;
import static net.java.games.input.Component.Identifier.Button.Key;

public class HoriTrackDescriptor extends DeviceDescriptor {

    public static final int A = 0;
    public static final int B = 1;
    public static final int Select = 2;
    public static final int Start = 3;
    public static final int Up = 4;
    public static final int Down = 5;
    public static final int Left = 6;
    public static final int Right = 7;

    public static final int AutofireA = 8;
    public static final int AutofireB = 9;
    public static final int ToggleAutofire = 10;

    public static final int ToggleSpeed = 11;
    public static final int ToggleOrientation = 12;

    public static final int RewindTime = 13;

    private static final Component.Identifier[] DEFAULTS = {
            Button.LEFT, Button.RIGHT, Key.APOSTROPHE, Key.RETURN,
            Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT,
            Key.S, Key.A, Key.Q,
            Key.P, Key.O,
            Key.EQUALS,
    };

    private static final int[][] SPEED_THRESHOLDS = {
            {0, 4, 8, 16, 24,}, // high
            {1, 16, 32, 48, 56,}, // low
    };

    private final Autofire autofire = new Autofire(2);

    private boolean toggleOrientationHeld;
    private boolean orientedLeft;

    private boolean toggleSpeedHeld;
    private boolean lowSpeed;

    private int mouseX = 127;
    private int mouseY = 119;

    private volatile boolean allowImpossibleInput;

    public HoriTrackDescriptor() {
        super(InputDevices.HoriTrack);
    }

    @Override
    public void handleSettingsChange(final Inputs inputs) {
        super.handleSettingsChange(inputs);
        allowImpossibleInput = inputs.isAllowImpossibleInput();
        autofire.setRate(inputs.getAutofireRate() - 1);
    }

    @Override
    public String getDeviceName() {
        return "Hori Track";
    }

    @Override
    public int getButtonCount() {
        return 14;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case A:
                return "A";
            case B:
                return "B";
            case Select:
                return "Select";
            case Start:
                return "Start";
            case Up:
                return "Up";
            case Down:
                return "Down";
            case Left:
                return "Left";
            case Right:
                return "Right";
            case AutofireA:
                return "Autofire A";
            case AutofireB:
                return "Autofire B";
            case ToggleAutofire:
                return "Toggle Autofire";
            case ToggleSpeed:
                return "Toggle Speed";
            case ToggleOrientation:
                return "Toggle Orientation";
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping((buttonIndex == A || buttonIndex == B)
                        ? InputUtil.getDefaultMouse() : InputUtil.getDefaultKeyboard(),
                buttonIndex, DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        autofire.setToggle(pressedValues[ToggleAutofire] != 0);
        autofire.buttonStates[0].update(pressedValues[AutofireA] != 0,
                pressedValues[A] != 0);
        autofire.buttonStates[1].update(pressedValues[AutofireB] != 0,
                pressedValues[B] != 0);

        final boolean toggleOrientation = pressedValues[ToggleOrientation] != 0;
        if (toggleOrientation != toggleOrientationHeld) {
            toggleOrientationHeld = toggleOrientation;
            if (toggleOrientation) {
                orientedLeft = !orientedLeft;
            }
        }
        final boolean toggleSpeed = pressedValues[ToggleSpeed] != 0;
        if (toggleSpeed != toggleSpeedHeld) {
            toggleSpeedHeld = toggleSpeed;
            if (toggleSpeed) {
                lowSpeed = !lowSpeed;
            }
        }

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        int buttons = 0;
        if (allowImpossibleInput) {
            if (pressedValues[Right] != 0) {
                buttons |= 0x0080_0000;
            }
            if (pressedValues[Left] != 0) {
                buttons |= 0x0040_0000;
            }
            if (pressedValues[Down] != 0) {
                buttons |= 0x0020_0000;
            }
            if (pressedValues[Up] != 0) {
                buttons |= 0x0010_0000;
            }
        } else {
            if (pressedValues[Right] > pressedValues[Left]) {
                buttons |= 0x0080_0000;
            }
            if (pressedValues[Left] > pressedValues[Right]) {
                buttons |= 0x0040_0000;
            }
            if (pressedValues[Down] > pressedValues[Up]) {
                buttons |= 0x0020_0000;
            }
            if (pressedValues[Up] > pressedValues[Down]) {
                buttons |= 0x0010_0000;
            }
        }
        if (pressedValues[Start] != 0) {
            buttons |= 0x0008_0000;
        }
        if (pressedValues[Select] != 0) {
            buttons |= 0x0004_0000;
        }
        if (lowSpeed) {
            buttons |= 0x0000_0800;
        }
        if (orientedLeft) {
            buttons |= 0x0000_0400;
        }

        if (autofire.buttonStates[1].asserted
                || (!autofire.enabled && pressedValues[B] != 0)) {
            buttons |= 0x0002_0000;
        }
        if (autofire.buttonStates[0].asserted
                || (!autofire.enabled && pressedValues[A] != 0)) {
            buttons |= 0x0001_0000;
        }

        final int[] thresholds = SPEED_THRESHOLDS[lowSpeed ? 1 : 0];

        int deltaX = mouseX;
        int deltaY = mouseY;
        final int mouse = InputUtil.getMouseCoordinates();
        final int y = (mouse >> 8) & 0xFF;
        if (y < 240) {
            mouseX = mouse & 0xFF;
            mouseY = y;
        }
        deltaX -= mouseX;
        deltaY -= mouseY;

        if (deltaX > thresholds[0]) {
            buttons |= (deltaX >= thresholds[4]) ? 0x0100_0000
                    : (deltaX >= thresholds[3]) ? 0x0900_0000
                    : (deltaX >= thresholds[2]) ? 0x0500_0000
                    : (deltaX >= thresholds[1]) ? 0x0300_0000
                    : 0x0700_0000;

        } else if (deltaX < -thresholds[0]) {
            buttons |= (deltaX <= -thresholds[4]) ? 0x0600_0000
                    : (deltaX <= -thresholds[3]) ? 0x0200_0000
                    : (deltaX <= -thresholds[2]) ? 0x0400_0000
                    : (deltaX <= -thresholds[1]) ? 0x0800_0000
                    : 0x0000_0000;
        } else {
            buttons |= 0x0F00_0000;
        }

        if (deltaY > thresholds[0]) {
            buttons |= (deltaY >= thresholds[4]) ? 0x6000_0000
                    : (deltaY >= thresholds[3]) ? 0x2000_0000
                    : (deltaY >= thresholds[2]) ? 0x4000_0000
                    : (deltaY >= thresholds[1]) ? 0x8000_0000
                    : 0x0000_0000;
        } else if (deltaY < -thresholds[0]) {
            buttons |= (deltaY <= -thresholds[4]) ? 0x1000_0000
                    : (deltaY <= -thresholds[3]) ? 0x9000_0000
                    : (deltaY <= -thresholds[2]) ? 0x5000_0000
                    : (deltaY <= -thresholds[1]) ? 0x3000_0000
                    : 0x7000_0000;
        } else {
            buttons |= 0xF000_0000;
        }

        return buttons | bits;
    }
}