package cn.kinlon.emu.input.gamepad;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Gamepad4;

public class Gamepad4Config extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public Gamepad4Config() {
        super(Gamepad4);
    }

    public Gamepad4Config(final Gamepad4Config gamepad4Config) {
        super(gamepad4Config);
    }

    @Override
    public Gamepad4Config copy() {
        return new Gamepad4Config(this);
    }
}
