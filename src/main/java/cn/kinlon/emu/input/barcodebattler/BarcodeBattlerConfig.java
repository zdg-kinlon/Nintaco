package cn.kinlon.emu.input.barcodebattler;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.BarcodeBattler;

public class BarcodeBattlerConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public BarcodeBattlerConfig() {
        super(BarcodeBattler);
    }

    public BarcodeBattlerConfig(final BarcodeBattlerConfig barcodeBattlerConfig) {
        super(barcodeBattlerConfig);
    }

    @Override
    public BarcodeBattlerConfig copy() {
        return new BarcodeBattlerConfig(this);
    }
}
