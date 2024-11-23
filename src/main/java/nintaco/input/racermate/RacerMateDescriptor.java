package nintaco.input.racermate;

import net.java.games.input.Component;
import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputUtil;

public abstract class RacerMateDescriptor extends DeviceDescriptor {

    public static final int LeftPedal = 0;
    public static final int RightPedal = 1;
    public static final int F1 = 2;
    public static final int F2 = 3;
    public static final int F3 = 4;
    public static final int Plus = 5;
    public static final int Minus = 6;
    public static final int Reset = 7;

    public static final int RewindTime = 8;
    public static final int HighSpeed = 9;

    public RacerMateDescriptor(final int inputDevice) {
        super(inputDevice);
    }

    @Override
    public int getButtonCount() {
        return 10;
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
            case LeftPedal:
                return "Left Pedal";
            case RightPedal:
                return "Right Pedal";
            case F1:
                return "F1 / Start";
            case F2:
                return "F2 / Display";
            case F3:
                return "F3 / Set";
            case Plus:
                return "+";
            case Minus:
                return "-";
            case Reset:
                return "Reset / Stop";
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

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);
        updateHighSpeed(pressedValues[HighSpeed] != 0, portIndex);

        int buttons = 0;

        if (pressedValues[LeftPedal] != 0) {
            buttons |= 0x80;
        }
        if (pressedValues[RightPedal] != 0) {
            buttons |= 0x40;
        }
        if (pressedValues[Minus] != 0) {
            buttons |= 0x20;
        }
        if (pressedValues[F2] != 0) {
            buttons |= 0x10;
        }
        if (pressedValues[Plus] != 0) {
            buttons |= 0x08;
        }
        if (pressedValues[F3] != 0) {
            buttons |= 0x04;
        }
        if (pressedValues[F1] != 0) {
            buttons |= 0x02;
        }
        if (pressedValues[Reset] != 0) {
            buttons |= 0x01;
        }

        return bits | (buttons << (portIndex << 3));
    }
}