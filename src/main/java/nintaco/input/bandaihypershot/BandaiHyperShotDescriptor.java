package nintaco.input.bandaihypershot;

import net.java.games.input.Component;
import nintaco.input.*;

import static net.java.games.input.Component.Identifier.Button.Button;
import static net.java.games.input.Component.Identifier.Button.Key;

public class BandaiHyperShotDescriptor extends DeviceDescriptor {

    public static final int Trigger = 0;
    public static final int Grenade = 1;
    public static final int OffscreenReload = 2;
    public static final int Select = 3;
    public static final int Start = 4;
    public static final int Up = 5;
    public static final int Down = 6;
    public static final int Left = 7;
    public static final int Right = 8;

    public static final int RewindTime = 9;

    private static final Component.Identifier[] DEFAULTS = {
            Button.LEFT, Button.RIGHT, Button.MIDDLE, Key.APOSTROPHE, Key.RETURN,
            Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT,
            Key.EQUALS,
    };

    private volatile boolean allowImpossibleInput;

    public BandaiHyperShotDescriptor() {
        super(InputDevices.BandaiHyperShot);
    }

    @Override
    public void handleSettingsChange(final Inputs inputs) {
        super.handleSettingsChange(inputs);
        allowImpossibleInput = inputs.isAllowImpossibleInput();
    }

    @Override
    public String getDeviceName() {
        return "Bandai Hyper Shot";
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
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case Trigger:
                return "Trigger";
            case Grenade:
                return "Grenade";
            case OffscreenReload:
                return "Offscreen Reload";
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
        return getDefaultButtonMapping((buttonIndex <= OffscreenReload)
                        ? InputUtil.getDefaultMouse() : InputUtil.getDefaultKeyboard(),
                buttonIndex, DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        final int deltaWheel = InputUtil.getMouseDeltaWheel();
        final boolean up = deltaWheel > 0;
        final boolean down = deltaWheel < 0;

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        int mouseCoordinates = InputUtil.getMouseCoordinates();
        int buttons = 0;
        if (allowImpossibleInput) {
            if (pressedValues[Right] != 0) {
                buttons |= 0x80;
            }
            if (pressedValues[Left] != 0) {
                buttons |= 0x40;
            }
            if (down || pressedValues[Down] != 0) {
                buttons |= 0x20;
            }
            if (up || pressedValues[Up] != 0) {
                buttons |= 0x10;
            }
        } else {
            if (pressedValues[Right] > pressedValues[Left]) {
                buttons |= 0x80;
            } else if (pressedValues[Left] > pressedValues[Right]) {
                buttons |= 0x40;
            }
            if (down || pressedValues[Down] > pressedValues[Up]) {
                buttons |= 0x20;
            } else if (up || pressedValues[Up] > pressedValues[Down]) {
                buttons |= 0x10;
            }
        }
        if (pressedValues[Start] != 0) {
            buttons |= 0x08;
        }
        if (pressedValues[Select] != 0) {
            buttons |= 0x04;
        }
        if (mouseCoordinates != 0xFFFF) {
            if (pressedValues[Grenade] != 0) {
                buttons |= 0x02;
            }
            if (pressedValues[Trigger] != 0) {
                buttons |= 0x01;
            } else if (pressedValues[OffscreenReload] != 0) {
                buttons |= 0x01;
                mouseCoordinates = 0xFFFF;
            }
        }

        return bits
                | (mouseCoordinates << 16)
                | (buttons << 8);
    }
}
