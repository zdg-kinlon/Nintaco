package cn.kinlon.emu.input.crazyclimber;

import cn.kinlon.emu.input.gamepad.GamepadConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.CrazyClimberLeft;

public class CrazyClimberLeftConfig
        extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public CrazyClimberLeftConfig() {
        super(CrazyClimberLeft);
    }

    public CrazyClimberLeftConfig(
            final CrazyClimberLeftConfig crazyClimberLeftConfig) {
        super(crazyClimberLeftConfig);
    }

    @Override
    public CrazyClimberLeftConfig copy() {
        return new CrazyClimberLeftConfig(this);
    }
}
