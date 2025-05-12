package cn.kinlon.emu.input.gamepad;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.InputDevices;

import static net.java.games.input.Component.Identifier.Key.*;

public class Gamepad3Descriptor extends GamepadDescriptor {

    private static final Key[] DEFAULTS = {N, Key.B, SLASH, RCONTROL, NUMPAD8,
            NUMPAD5, NUMPAD4, NUMPAD6, H, G, T, LBRACKET, W};

    public Gamepad3Descriptor() {
        super(InputDevices.Gamepad3);
    }

    @Override
    public String getDeviceName() {
        return "Gamepad 3";
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex, DEFAULTS);
    }
}
