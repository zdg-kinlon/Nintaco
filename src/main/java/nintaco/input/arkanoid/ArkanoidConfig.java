package nintaco.input.arkanoid;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Arkanoid;

public class ArkanoidConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public ArkanoidConfig() {
        super(Arkanoid);
    }

    public ArkanoidConfig(final List<ButtonMapping> buttonMappings) {
        super(Arkanoid, buttonMappings);
    }

    public ArkanoidConfig(final ArkanoidConfig arkanoidConfig) {
        super(arkanoidConfig);
    }

    @Override
    public ArkanoidConfig copy() {
        return new ArkanoidConfig(this);
    }
}
