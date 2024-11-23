package nintaco.input.crazyclimber;

import nintaco.input.gamepad.GamepadDescriptor;

public abstract class CrazyClimberDescriptor extends GamepadDescriptor {

    public static final int RewindTime = 8;
    public static final int HighSpeed = 9;

    public CrazyClimberDescriptor(final int inputDevice) {
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
            case A:
                return "A";
            case B:
                return "B";
            case Select:
                return "Select";
            case Start:
                return "Start";
            case Up:
                return "Right";
            case Down:
                return "Left";
            case Left:
                return "Up";
            case Right:
                return "Down";
            case RewindTime:
                return "Rewind Time";
            case HighSpeed:
                return "High Speed";
            default:
                return "Unknown";
        }
    }
}
