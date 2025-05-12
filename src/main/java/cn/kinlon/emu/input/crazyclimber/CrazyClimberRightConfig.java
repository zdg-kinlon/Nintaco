package cn.kinlon.emu.input.crazyclimber;

import cn.kinlon.emu.input.gamepad.GamepadConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.CrazyClimberRight;

public class CrazyClimberRightConfig
        extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public CrazyClimberRightConfig() {
        super(CrazyClimberRight);
    }

    public CrazyClimberRightConfig(
            final CrazyClimberRightConfig crazyClimberRightConfig) {
        super(crazyClimberRightConfig);
    }

    @Override
    public CrazyClimberRightConfig copy() {
        return new CrazyClimberRightConfig(this);
    }
}
