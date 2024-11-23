package nintaco.input.mahjong;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Mahjong;

public class MahjongConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public MahjongConfig() {
        super(Mahjong);
    }

    public MahjongConfig(final List<ButtonMapping> buttonMappings) {
        super(Mahjong, buttonMappings);
    }

    public MahjongConfig(final MahjongConfig mahjongConfig) {
        super(mahjongConfig);
    }

    @Override
    public MahjongConfig copy() {
        return new MahjongConfig(this);
    }
}

