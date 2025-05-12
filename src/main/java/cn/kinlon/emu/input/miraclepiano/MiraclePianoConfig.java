package cn.kinlon.emu.input.miraclepiano;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.MiraclePiano;

public class MiraclePianoConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public MiraclePianoConfig() {
        super(MiraclePiano);
    }

    public MiraclePianoConfig(final MiraclePianoConfig miraclePianoConfig) {
        super(miraclePianoConfig);
    }

    @Override
    public MiraclePianoConfig copy() {
        return new MiraclePianoConfig(this);
    }
}