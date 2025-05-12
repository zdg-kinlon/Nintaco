package cn.kinlon.emu.input.gamepad;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class Gamepad2Descriptor extends GamepadDescriptor {

    private static final Key[] DEFAULTS
            = {V, C, RBRACKET, BACKSLASH, I, K, J, L, F, D, E, MINUS, TAB};

    public Gamepad2Descriptor() {
        super(InputDevices.Gamepad2);
    }

    @Override
    public String getDeviceName() {
        return "Gamepad 2";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}
