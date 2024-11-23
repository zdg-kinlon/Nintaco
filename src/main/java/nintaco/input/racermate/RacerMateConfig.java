package nintaco.input.racermate;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

public abstract class RacerMateConfig extends DeviceConfig
        implements Serializable {

    private static final long serialVersionUID = 0;

    public RacerMateConfig(final int inputDevice) {
        super(inputDevice);
    }

    public RacerMateConfig(final int inputDevice,
                           final List<ButtonMapping> buttonMappings) {
        super(inputDevice, buttonMappings);
    }

    public RacerMateConfig(final RacerMateConfig racermateConfig) {
        super(racermateConfig);
    }
}
