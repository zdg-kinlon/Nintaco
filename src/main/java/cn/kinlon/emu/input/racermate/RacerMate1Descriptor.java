package cn.kinlon.emu.input.racermate;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class RacerMate1Descriptor extends RacerMateDescriptor {

    private static final Key[] DEFAULTS = {Z, X, RETURN, APOSTROPHE, SEMICOLON,
            UP, DOWN, R, BACK, GRAVE};

    public RacerMate1Descriptor() {
        super(InputDevices.RacerMate1);
    }

    @Override
    public String getDeviceName() {
        return "RacerMate CompuTrainer 1";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}
