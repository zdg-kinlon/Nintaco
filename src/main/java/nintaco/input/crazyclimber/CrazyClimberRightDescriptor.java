package nintaco.input.crazyclimber;

import nintaco.input.ButtonMapping;
import nintaco.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class CrazyClimberRightDescriptor extends CrazyClimberDescriptor {

    private static final Key[] DEFAULTS
            = {V, C, RBRACKET, BACKSLASH, L, J, I, K, EQUALS, TAB};

    public CrazyClimberRightDescriptor() {
        super(InputDevices.CrazyClimberRight);
    }

    @Override
    public String getDeviceName() {
        return "Crazy Climber Right";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}

