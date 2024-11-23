package nintaco.input.zapper;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Zapper;

public class ZapperConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public ZapperConfig() {
        super(Zapper);
    }

    public ZapperConfig(final List<ButtonMapping> buttonMappings) {
        super(Zapper, buttonMappings);
    }

    public ZapperConfig(final ZapperConfig zapperConfig) {
        super(zapperConfig);
    }

    @Override
    public ZapperConfig copy() {
        return new ZapperConfig(this);
    }
}
