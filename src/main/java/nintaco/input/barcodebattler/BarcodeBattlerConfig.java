package nintaco.input.barcodebattler;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.BarcodeBattler;

public class BarcodeBattlerConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public BarcodeBattlerConfig() {
        super(BarcodeBattler);
    }

    public BarcodeBattlerConfig(final List<ButtonMapping> buttonMappings) {
        super(BarcodeBattler, buttonMappings);
    }

    public BarcodeBattlerConfig(final BarcodeBattlerConfig barcodeBattlerConfig) {
        super(barcodeBattlerConfig);
    }

    @Override
    public BarcodeBattlerConfig copy() {
        return new BarcodeBattlerConfig(this);
    }
}
