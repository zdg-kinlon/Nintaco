package cn.kinlon.emu.input.gamepad;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class Gamepad1Descriptor extends GamepadDescriptor {

    private static final Key[] DEFAULTS = {X, Z, APOSTROPHE, RETURN, UP, DOWN,
            LEFT, RIGHT, S, Key.A, Q, BACK, GRAVE};

    public Gamepad1Descriptor() {
        super(InputDevices.Gamepad1);
    }

    @Override
    public String getDeviceName() {
        return "Gamepad 1";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}
