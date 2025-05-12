package cn.kinlon.emu.input.uforce;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.UForce;

public class UForceConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public UForceConfig() {
        super(UForce);
    }

    public UForceConfig(final UForceConfig uForceConfig) {
        super(uForceConfig);
    }

    @Override
    public UForceConfig copy() {
        return new UForceConfig(this);
    }
}