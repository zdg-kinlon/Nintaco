package nintaco.input.excitingboxing;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class ExcitingBoxingDescriptor extends DeviceDescriptor {

    public static final int LeftHook = 0;
    public static final int RightHook = 1;
    public static final int LeftJab = 2;
    public static final int RightJab = 3;
    public static final int Straight = 4;
    public static final int Body = 5;
    public static final int MoveLeft = 6;
    public static final int MoveRight = 7;

    public static final int RewindTime = 8;

    private static final Key[] DEFAULTS
            = {R, Y, F, H, T, G, LEFT, RIGHT, SPACE};

    public ExcitingBoxingDescriptor() {
        super(InputDevices.ExcitingBoxing);
    }

    @Override
    public String getDeviceName() {
        return "Exciting Boxing";
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
            case LeftHook:
                return "Left Hook";
            case RightHook:
                return "Right Hook";
            case LeftJab:
                return "Left Jab";
            case RightJab:
                return "Right Jab";
            case Straight:
                return "Straight";
            case Body:
                return "Body";
            case MoveLeft:
                return "Move Left";
            case MoveRight:
                return "Move Right";
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
    public int setButtonBits(int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        // 23 22 21 20  19 18 17 16

        if (pressedValues[LeftHook] != 0) {
            bits |= 0x00_01_00_00;
        }
        if (pressedValues[RightHook] != 0) {
            bits |= 0x00_02_00_00;
        }
        if (pressedValues[LeftJab] != 0) {
            bits |= 0x00_04_00_00;
        }
        if (pressedValues[RightJab] != 0) {
            bits |= 0x00_08_00_00;
        }
        if (pressedValues[Straight] != 0) {
            bits |= 0x00_10_00_00;
        }
        if (pressedValues[Body] != 0) {
            bits |= 0x00_20_00_00;
        }
        if (pressedValues[MoveLeft] != 0) {
            bits |= 0x00_40_00_00;
        }
        if (pressedValues[MoveRight] != 0) {
            bits |= 0x00_80_00_00;
        }

        return bits;
    }
}