package cn.kinlon.emu.input.oekakids;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.OekaKids;

public class OekaKidsConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public OekaKidsConfig() {
        super(OekaKids);
    }

    public OekaKidsConfig(final OekaKidsConfig oekaKidsConfig) {
        super(oekaKidsConfig);
    }

    @Override
    public OekaKidsConfig copy() {
        return new OekaKidsConfig(this);
    }
}