package cn.kinlon.emu.input.gamepad;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Gamepad1;

public class Gamepad1Config extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public Gamepad1Config() {
        super(Gamepad1);
    }

    public Gamepad1Config(final Gamepad1Config gamepad1Config) {
        super(gamepad1Config);
    }

    @Override
    public Gamepad1Config copy() {
        return new Gamepad1Config(this);
    }
}
