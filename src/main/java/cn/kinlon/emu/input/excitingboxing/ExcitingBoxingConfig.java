package cn.kinlon.emu.input.excitingboxing;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.ExcitingBoxing;

public class ExcitingBoxingConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public ExcitingBoxingConfig() {
        super(ExcitingBoxing);
    }

    public ExcitingBoxingConfig(final ExcitingBoxingConfig excitingBoxingConfig) {
        super(excitingBoxingConfig);
    }

    @Override
    public ExcitingBoxingConfig copy() {
        return new ExcitingBoxingConfig(this);
    }
}
