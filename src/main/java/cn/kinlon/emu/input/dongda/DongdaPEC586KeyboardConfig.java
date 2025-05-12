package cn.kinlon.emu.input.dongda;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.DongdaPEC586Keyboard;

public class DongdaPEC586KeyboardConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public DongdaPEC586KeyboardConfig() {
        super(DongdaPEC586Keyboard);
    }

    public DongdaPEC586KeyboardConfig(
            final DongdaPEC586KeyboardConfig dongaPEC586KeyboardConfig) {
        super(dongaPEC586KeyboardConfig);
    }

    @Override
    public DongdaPEC586KeyboardConfig copy() {
        return new DongdaPEC586KeyboardConfig(this);
    }
}