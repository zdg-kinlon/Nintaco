package cn.kinlon.emu.input.gamepad;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Gamepad2;

public class Gamepad2Config extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public Gamepad2Config() {
        super(Gamepad2);
    }

    public Gamepad2Config(final Gamepad2Config gamepad2Config) {
        super(gamepad2Config);
    }

    @Override
    public Gamepad2Config copy() {
        return new Gamepad2Config(this);
    }
}
