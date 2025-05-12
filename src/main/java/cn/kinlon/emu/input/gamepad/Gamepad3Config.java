package cn.kinlon.emu.input.gamepad;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Gamepad3;

public class Gamepad3Config extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public Gamepad3Config() {
        super(Gamepad3);
    }

    public Gamepad3Config(final Gamepad3Config gamepad3Config) {
        super(gamepad3Config);
    }

    @Override
    public Gamepad3Config copy() {
        return new Gamepad3Config(this);
    }
}
