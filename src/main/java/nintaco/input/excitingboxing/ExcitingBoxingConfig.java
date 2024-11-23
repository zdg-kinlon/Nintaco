package nintaco.input.excitingboxing;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.ExcitingBoxing;

public class ExcitingBoxingConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public ExcitingBoxingConfig() {
        super(ExcitingBoxing);
    }

    public ExcitingBoxingConfig(final List<ButtonMapping> buttonMappings) {
        super(ExcitingBoxing, buttonMappings);
    }

    public ExcitingBoxingConfig(final ExcitingBoxingConfig excitingBoxingConfig) {
        super(excitingBoxingConfig);
    }

    @Override
    public ExcitingBoxingConfig copy() {
        return new ExcitingBoxingConfig(this);
    }
}
