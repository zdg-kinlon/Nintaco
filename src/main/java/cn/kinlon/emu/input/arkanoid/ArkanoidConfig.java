package cn.kinlon.emu.input.arkanoid;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Arkanoid;

public class ArkanoidConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public ArkanoidConfig() {
        super(Arkanoid);
    }

    public ArkanoidConfig(final ArkanoidConfig arkanoidConfig) {
        super(arkanoidConfig);
    }

    @Override
    public ArkanoidConfig copy() {
        return new ArkanoidConfig(this);
    }
}
