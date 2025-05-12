package cn.kinlon.emu.input.subor;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Subor;

public class SuborConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public SuborConfig() {
        super(Subor);
    }

    public SuborConfig(final SuborConfig suborConfig) {
        super(suborConfig);
    }

    @Override
    public SuborConfig copy() {
        return new SuborConfig(this);
    }
}
