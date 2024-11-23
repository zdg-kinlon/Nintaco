package nintaco.input.crazyclimber;

import nintaco.input.ButtonMapping;
import nintaco.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class CrazyClimberLeftDescriptor extends CrazyClimberDescriptor {

    private static final Key[] DEFAULTS
            = {X, Z, APOSTROPHE, RETURN, D, Key.A, W, S, BACK, GRAVE};

    public CrazyClimberLeftDescriptor() {
        super(InputDevices.CrazyClimberLeft);
    }

    @Override
    public String getDeviceName() {
        return "Crazy Climber Left";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}

