package nintaco.input.powerglove;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.PowerGlove;

public class PowerGloveConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PowerGloveConfig() {
        super(PowerGlove);
    }

    public PowerGloveConfig(final List<ButtonMapping> buttonMappings) {
        super(PowerGlove, buttonMappings);
    }

    public PowerGloveConfig(final PowerGloveConfig powerGloveConfig) {
        super(powerGloveConfig);
    }

    @Override
    public PowerGloveConfig copy() {
        return new PowerGloveConfig(this);
    }
}