package cn.kinlon.emu.input.snesmouse;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.SnesMouse;

public class SnesMouseConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public SnesMouseConfig() {
        super(SnesMouse);
    }

    public SnesMouseConfig(final SnesMouseConfig snesMouseConfig) {
        super(snesMouseConfig);
    }

    @Override
    public SnesMouseConfig copy() {
        return new SnesMouseConfig(this);
    }
}