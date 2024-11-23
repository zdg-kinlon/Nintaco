package nintaco.input.glasses;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Glasses;

public class GlassesConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public GlassesConfig() {
        super(Glasses);
    }

    public GlassesConfig(final List<ButtonMapping> buttonMappings) {
        super(Glasses, buttonMappings);
    }

    public GlassesConfig(final GlassesConfig glassesConfig) {
        super(glassesConfig);
    }

    @Override
    public GlassesConfig copy() {
        return new GlassesConfig(this);
    }
}
