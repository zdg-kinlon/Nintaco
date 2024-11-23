package nintaco.input.arkanoid;

import net.java.games.input.Component;
import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;

import static net.java.games.input.Component.Identifier.Button.LEFT;
import static net.java.games.input.Component.Identifier.Button.RIGHT;

public class ArkanoidDescriptor extends DeviceDescriptor {

    public static final int Fire = 0;

    public static final int RewindTime = 1;

    private static final Component.Identifier.Button[] DEFAULTS = {LEFT, RIGHT};

    public ArkanoidDescriptor() {
        super(InputDevices.Arkanoid);
    }

    @Override
    public String getDeviceName() {
        return "Arkanoid Vaus";
    }

    @Override
    public int getButtonCount() {
        return 2;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case Fire:
                return "Fire";
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(InputUtil.getDefaultMouse(), buttonIndex,
                DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        final int mouseCoordinates = InputUtil.getMouseCoordinates();

        int buttons = 0;
        if (mouseCoordinates != 0xFFFF && pressedValues[Fire] != 0) {
            buttons |= 0x04;
        }

        return bits
                | (InputUtil.getMouseCoordinates() << 16)
                | (portIndex == 0 ? buttons : (buttons << 8));
    }
}
