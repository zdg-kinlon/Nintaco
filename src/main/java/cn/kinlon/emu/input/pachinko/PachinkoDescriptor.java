package cn.kinlon.emu.input.pachinko;

import cn.kinlon.emu.input.*;
import static net.java.games.input.Component.Identifier.Key.*;
import static cn.kinlon.emu.input.ConsoleType.Famicom;
import static cn.kinlon.emu.input.Ports.Port2;

public class PachinkoDescriptor extends DeviceDescriptor {

    public static final int A = 0;
    public static final int B = 1;
    public static final int Select = 2;
    public static final int Start = 3;
    public static final int Up = 4;
    public static final int Down = 5;
    public static final int Left = 6;
    public static final int Right = 7;

    public static final int RewindTime = 8;

    private static final Key[] DEFAULTS
            = {X, Z, APOSTROPHE, RETURN, UP, DOWN, LEFT, RIGHT, EQUALS,};

    private volatile boolean allowImpossibleInput;

    public PachinkoDescriptor() {
        super(InputDevices.Pachinko);
    }

    @Override
    public void handleSettingsChange(final Inputs inputs) {
        super.handleSettingsChange(inputs);
        allowImpossibleInput = inputs.isAllowImpossibleInput();
    }

    @Override
    public String getDeviceName() {
        return "Pachinko";
    }

    @Override
    public int getButtonCount() {
        return 9;
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
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(InputUtil.getDefaultKeyboard(), buttonIndex,
                DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        int buttons = 0;
        final float wheel = InputUtil.getMouseDeltaWheel();
        if (wheel < 0f) {
            buttons |= 0x02_00;
        } else if (wheel > 0f) {
            buttons |= 0x01_00;
        }
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
            }
            if (pressedValues[Left] > pressedValues[Right]) {
                buttons |= 0x40;
            }
            if (pressedValues[Down] > pressedValues[Up]) {
                buttons |= 0x20;
            }
            if (pressedValues[Up] > pressedValues[Down]) {
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
        if (pressedValues[B] != 0) {
            buttons |= 0x02;
        }
        if (pressedValues[A] != 0) {
            buttons |= 0x01;
        }

        return (buttons << 16) | bits;
    }
}