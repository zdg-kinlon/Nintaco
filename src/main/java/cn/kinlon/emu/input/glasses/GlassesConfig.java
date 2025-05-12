package cn.kinlon.emu.input.glasses;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Glasses;

public class GlassesConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public GlassesConfig() {
        super(Glasses);
    }

    public GlassesConfig(final GlassesConfig glassesConfig) {
        super(glassesConfig);
    }

    @Override
    public GlassesConfig copy() {
        return new GlassesConfig(this);
    }
}
