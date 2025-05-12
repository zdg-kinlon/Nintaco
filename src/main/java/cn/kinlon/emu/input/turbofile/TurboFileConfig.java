package cn.kinlon.emu.input.turbofile;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.TurboFile;

public class TurboFileConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TurboFileConfig() {
        super(TurboFile);
    }

    public TurboFileConfig(final TurboFileConfig oekaKidsConfig) {
        super(oekaKidsConfig);
    }

    @Override
    public TurboFileConfig copy() {
        return new TurboFileConfig(this);
    }
}
