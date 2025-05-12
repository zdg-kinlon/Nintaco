package cn.kinlon.emu.input.mahjong;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Mahjong;

public class MahjongConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public MahjongConfig() {
        super(Mahjong);
    }

    public MahjongConfig(final MahjongConfig mahjongConfig) {
        super(mahjongConfig);
    }

    @Override
    public MahjongConfig copy() {
        return new MahjongConfig(this);
    }
}

