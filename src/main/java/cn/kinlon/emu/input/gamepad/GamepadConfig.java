package cn.kinlon.emu.input.gamepad;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

public abstract class GamepadConfig extends DeviceConfig
        implements Serializable {

    private static final long serialVersionUID = 0;

    public GamepadConfig(final int inputDevice) {
        super(inputDevice);
    }

    public GamepadConfig(final int inputDevice,
                         final List<ButtonMapping> buttonMappings) {
        super(inputDevice, buttonMappings);
    }

    public GamepadConfig(final GamepadConfig gamepadConfig) {
        super(gamepadConfig);
    }
}
