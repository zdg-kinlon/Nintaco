package cn.kinlon.emu.input.powerpad;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.PowerPad;

public class PowerPadConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PowerPadConfig() {
        super(PowerPad);
    }

    public PowerPadConfig(final PowerPadConfig powerPadConfig) {
        super(powerPadConfig);
    }

    @Override
    public PowerPadConfig copy() {
        return new PowerPadConfig(this);
    }
}
