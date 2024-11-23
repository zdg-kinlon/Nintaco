package nintaco.input.uforce;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.UForce;

public class UForceConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public UForceConfig() {
        super(UForce);
    }

    public UForceConfig(final List<ButtonMapping> buttonMappings) {
        super(UForce, buttonMappings);
    }

    public UForceConfig(final UForceConfig uForceConfig) {
        super(uForceConfig);
    }

    @Override
    public UForceConfig copy() {
        return new UForceConfig(this);
    }
}