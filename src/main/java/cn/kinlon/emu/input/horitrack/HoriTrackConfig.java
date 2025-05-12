package cn.kinlon.emu.input.horitrack;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.HoriTrack;

public class HoriTrackConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public HoriTrackConfig() {
        super(HoriTrack);
    }

    public HoriTrackConfig(final HoriTrackConfig horiTrackConfig) {
        super(horiTrackConfig);
    }

    @Override
    public HoriTrackConfig copy() {
        return new HoriTrackConfig(this);
    }
}