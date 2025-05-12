package cn.kinlon.emu.input.taptapmat;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.TapTapMat;

public class TapTapMatConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TapTapMatConfig() {
        super(TapTapMat);
    }

    public TapTapMatConfig(final TapTapMatConfig tapTapMatConfig) {
        super(tapTapMatConfig);
    }

    @Override
    public TapTapMatConfig copy() {
        return new TapTapMatConfig(this);
    }
}
