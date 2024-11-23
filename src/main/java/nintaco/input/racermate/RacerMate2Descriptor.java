package nintaco.input.racermate;

import nintaco.input.ButtonMapping;
import nintaco.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class RacerMate2Descriptor extends RacerMateDescriptor {

    private static final Key[] DEFAULTS = {C, V, BACKSLASH, RBRACKET, LBRACKET,
            EQUALS, MINUS, T, INSERT, TAB};

    public RacerMate2Descriptor() {
        super(InputDevices.RacerMate2);
    }

    @Override
    public String getDeviceName() {
        return "RacerMate CompuTrainer 2";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}
