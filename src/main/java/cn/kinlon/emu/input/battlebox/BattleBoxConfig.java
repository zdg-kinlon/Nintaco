package cn.kinlon.emu.input.battlebox;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.BattleBox;

public class BattleBoxConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public BattleBoxConfig() {
        super(BattleBox);
    }

    public BattleBoxConfig(final BattleBoxConfig battleBoxConfig) {
        super(battleBoxConfig);
    }

    @Override
    public BattleBoxConfig copy() {
        return new BattleBoxConfig(this);
    }
}