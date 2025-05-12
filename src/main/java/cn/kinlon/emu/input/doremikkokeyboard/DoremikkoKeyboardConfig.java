package cn.kinlon.emu.input.doremikkokeyboard;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.DoremikkoKeyboard;

public class DoremikkoKeyboardConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public DoremikkoKeyboardConfig() {
        super(DoremikkoKeyboard);
    }

    public DoremikkoKeyboardConfig(
            final DoremikkoKeyboardConfig doremikkoKeyboardConfig) {
        super(doremikkoKeyboardConfig);
    }

    @Override
    public DoremikkoKeyboardConfig copy() {
        return new DoremikkoKeyboardConfig(this);
    }
}