package cn.kinlon.emu.input.zapper;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Zapper;

public class ZapperConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public ZapperConfig() {
        super(Zapper);
    }

    public ZapperConfig(final ZapperConfig zapperConfig) {
        super(zapperConfig);
    }

    @Override
    public ZapperConfig copy() {
        return new ZapperConfig(this);
    }
}
