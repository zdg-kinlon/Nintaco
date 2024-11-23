package nintaco.input.subor;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Subor;

public class SuborConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public SuborConfig() {
        super(Subor);
    }

    public SuborConfig(final List<ButtonMapping> buttonMappings) {
        super(Subor, buttonMappings);
    }

    public SuborConfig(final SuborConfig suborConfig) {
        super(suborConfig);
    }

    @Override
    public SuborConfig copy() {
        return new SuborConfig(this);
    }
}
