package nintaco.input.gamepad;

import net.java.games.input.Component;
import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputUtil;
import nintaco.input.Inputs;

import static nintaco.input.ConsoleType.Famicom;
import static nintaco.input.Ports.Port2;

public abstract class GamepadDescriptor extends DeviceDescriptor {

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
    public static final int RewindTime = 11;
    public static final int HighSpeed = 12;

    private final Autofire autofire = new Autofire(2);
    private final int rewindTimeIndex;
    private final int highSpeedIndex;

    private volatile boolean allowImpossibleInput;

    public GamepadDescriptor(final int inputDevice) {
        super(inputDevice);
        rewindTimeIndex = getRewindTimeButton();
        highSpeedIndex = getHighSpeedButton();
    }

    @Override
    public void handleSettingsChange(final Inputs inputs) {
        super.handleSettingsChange(inputs);
        allowImpossibleInput = inputs.isAllowImpossibleInput();
        autofire.setRate(inputs.getAutofireRate() - 1);
    }

    @Override
    public int getButtonCount() {
        return 13;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public int getHighSpeedButton() {
        return HighSpeed;
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
            case RewindTime:
                return "Rewind Time";
            case HighSpeed:
                return "High Speed";
            default:
                return "Unknown";
        }
    }

    protected ButtonMapping getDefaultButtonMapping(final int buttonIndex,
                                                    final Component.Identifier[] defaults) {
        return getDefaultButtonMapping(InputUtil.getDefaultKeyboard(), buttonIndex,
                defaults);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        autofire.setToggle(pressedValues[ToggleAutofire] != 0);
        autofire.buttonStates[0].update(pressedValues[AutofireA] != 0,
                pressedValues[A] != 0);
        autofire.buttonStates[1].update(pressedValues[AutofireB] != 0,
                pressedValues[B] != 0);

        updateRewindTime(pressedValues[rewindTimeIndex] != 0, portIndex);
        updateHighSpeed(pressedValues[highSpeedIndex] != 0, portIndex);

        int buttons = 0;
        if (allowImpossibleInput) {
            if (pressedValues[Right] != 0) {
                buttons |= 0x80;
            }
            if (pressedValues[Left] != 0) {
                buttons |= 0x40;
            }
            if (pressedValues[Down] != 0) {
                buttons |= 0x20;
            }
            if (pressedValues[Up] != 0) {
                buttons |= 0x10;
            }
        } else {
            if (pressedValues[Right] > pressedValues[Left]) {
                buttons |= 0x80;
            } else if (pressedValues[Left] > pressedValues[Right]) {
                buttons |= 0x40;
            }
            if (pressedValues[Down] > pressedValues[Up]) {
                buttons |= 0x20;
            } else if (pressedValues[Up] > pressedValues[Down]) {
                buttons |= 0x10;
            }
        }
        if (!(consoleType == Famicom && portIndex == Port2)) {
            if (pressedValues[Start] != 0) {
                buttons |= 0x08;
            }
            if (pressedValues[Select] != 0) {
                buttons |= 0x04;
            }
        }

        if (autofire.buttonStates[1].asserted
                || (!autofire.enabled && pressedValues[B] != 0)) {
            buttons |= 0x02;
        }
        if (autofire.buttonStates[0].asserted
                || (!autofire.enabled && pressedValues[A] != 0)) {
            buttons |= 0x01;
        }

        return bits | (buttons << (portIndex << 3));
    }
}
