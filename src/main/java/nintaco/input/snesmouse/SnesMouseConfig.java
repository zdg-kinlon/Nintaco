package nintaco.input.snesmouse;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.SnesMouse;

public class SnesMouseConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public SnesMouseConfig() {
        super(SnesMouse);
    }

    public SnesMouseConfig(final List<ButtonMapping> buttonMappings) {
        super(SnesMouse, buttonMappings);
    }

    public SnesMouseConfig(final SnesMouseConfig snesMouseConfig) {
        super(snesMouseConfig);
    }

    @Override
    public SnesMouseConfig copy() {
        return new SnesMouseConfig(this);
    }
}