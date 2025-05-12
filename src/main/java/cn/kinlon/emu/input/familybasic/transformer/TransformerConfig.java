package cn.kinlon.emu.input.familybasic.transformer;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.TransformerKeyboard;

public class TransformerConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TransformerConfig() {
        super(TransformerKeyboard);
    }

    public TransformerConfig(final TransformerConfig transformerConfig) {
        super(transformerConfig);
    }

    @Override
    public TransformerConfig copy() {
        return new TransformerConfig(this);
    }
}
