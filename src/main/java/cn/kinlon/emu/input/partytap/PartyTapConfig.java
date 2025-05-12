package cn.kinlon.emu.input.partytap;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.PartyTap;

public class PartyTapConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PartyTapConfig() {
        super(PartyTap);
    }

    public PartyTapConfig(
            final PartyTapConfig partyTapConfig) {
        super(partyTapConfig);
    }

    @Override
    public PartyTapConfig copy() {
        return new PartyTapConfig(this);
    }
}