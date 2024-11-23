package nintaco.input.gamepad;

import nintaco.input.ButtonMapping;
import nintaco.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class Gamepad4Descriptor extends GamepadDescriptor {

    private static final Key[] DEFAULTS = {COMMA, M, LCONTROL, SPACE, HOME, END,
            DELETE, PAGEDOWN, U, Y, RSHIFT, PAGEUP, INSERT};

    public Gamepad4Descriptor() {
        super(InputDevices.Gamepad4);
    }

    @Override
    public String getDeviceName() {
        return "Gamepad 4";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}