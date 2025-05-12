package cn.kinlon.emu.input.powerglove;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.PowerGlove;

public class PowerGloveConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PowerGloveConfig() {
        super(PowerGlove);
    }

    public PowerGloveConfig(final PowerGloveConfig powerGloveConfig) {
        super(powerGloveConfig);
    }

    @Override
    public PowerGloveConfig copy() {
        return new PowerGloveConfig(this);
    }
}