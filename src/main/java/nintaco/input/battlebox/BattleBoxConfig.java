package nintaco.input.battlebox;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.BattleBox;

public class BattleBoxConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public BattleBoxConfig() {
        super(BattleBox);
    }

    public BattleBoxConfig(final List<ButtonMapping> buttonMappings) {
        super(BattleBox, buttonMappings);
    }

    public BattleBoxConfig(final BattleBoxConfig battleBoxConfig) {
        super(battleBoxConfig);
    }

    @Override
    public BattleBoxConfig copy() {
        return new BattleBoxConfig(this);
    }
}