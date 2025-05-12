package cn.kinlon.emu.input.pachinko;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Pachinko;

public class PachinkoConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PachinkoConfig() {
        super(Pachinko);
    }

    public PachinkoConfig(final PachinkoConfig pachinkoConfig) {
        super(pachinkoConfig);
    }

    @Override
    public PachinkoConfig copy() {
        return new PachinkoConfig(this);
    }
}
