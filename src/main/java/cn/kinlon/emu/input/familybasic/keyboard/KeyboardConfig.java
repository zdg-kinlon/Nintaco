package cn.kinlon.emu.input.familybasic.keyboard;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Keyboard;

public class KeyboardConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public KeyboardConfig() {
        super(Keyboard);
    }

    public KeyboardConfig(final KeyboardConfig keyboardConfig) {
        super(keyboardConfig);
    }

    @Override
    public KeyboardConfig copy() {
        return new KeyboardConfig(this);
    }
}
