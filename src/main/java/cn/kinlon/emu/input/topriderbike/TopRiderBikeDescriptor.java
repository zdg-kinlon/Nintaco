package cn.kinlon.emu.input.topriderbike;

import cn.kinlon.emu.input.*;

import static net.java.games.input.Component.Identifier.Key.*;

public class TopRiderBikeDescriptor extends DeviceDescriptor {

    public static final int Accelerate = 0;
    public static final int Brake = 1;
    public static final int Select = 2;
    public static final int Start = 3;
    public static final int ShiftGear = 4;
    public static final int Wheelie = 5;
    public static final int SteerLeft = 6;
    public static final int SteerRight = 7;

    public static final int RewindTime = 8;

    private static final Key[] DEFAULTS
            = {X, Z, APOSTROPHE, RETURN, UP, DOWN, LEFT, RIGHT, EQUALS,};

    private volatile boolean allowImpossibleInput;

    public TopRiderBikeDescriptor() {
        super(InputDevices.TopRiderBike);
    }

    @Override
    public void handleSettingsChange(final Inputs inputs) {
        super.handleSettingsChange(inputs);
        allowImpossibleInput = inputs.isAllowImpossibleInput();
    }

    @Override
    public String getDeviceName() {
        return "Top-Rider Bike";
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
            case Accelerate:
                return "Accelerate";
            case Brake:
                return "Brake";
            case Select:
                return "Select";
            case Start:
                return "Start";
            case ShiftGear:
                return "Shift Gear";
            case Wheelie:
                return "Wheelie";
            case SteerLeft:
                return "Steer Left";
            case SteerRight:
                return "Steer Right";
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
        if (allowImpossibleInput) {
            if (pressedValues[SteerRight] != 0) {
                buttons |= 0x80_0000;
            }
            if (pressedValues[SteerLeft] != 0) {
                buttons |= 0x40_0000;
            }
        } else {
            if (pressedValues[SteerRight] > pressedValues[SteerLeft]) {
                buttons |= 0x80_0000;
            }
            if (pressedValues[SteerLeft] > pressedValues[SteerRight]) {
                buttons |= 0x40_0000;
            }
        }
        if (pressedValues[Wheelie] != 0) {
            buttons |= 0x20_0000;
        }
        if (pressedValues[ShiftGear] != 0) {
            buttons |= 0x10_0000;
        }
        if (pressedValues[Start] != 0) {
            buttons |= 0x08_0000;
        }
        if (pressedValues[Select] != 0) {
            buttons |= 0x04_0000;
        }
        if (pressedValues[Brake] != 0) {
            buttons |= 0x02_0000;
        }
        if (pressedValues[Accelerate] != 0) {
            buttons |= 0x01_0000;
        }

        return buttons | bits;
    }
}
