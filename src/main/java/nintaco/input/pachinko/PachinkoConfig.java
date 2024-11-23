package nintaco.input.pachinko;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Pachinko;

public class PachinkoConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PachinkoConfig() {
        super(Pachinko);
    }

    public PachinkoConfig(final List<ButtonMapping> buttonMappings) {
        super(Pachinko, buttonMappings);
    }

    public PachinkoConfig(final PachinkoConfig pachinkoConfig) {
        super(pachinkoConfig);
    }

    @Override
    public PachinkoConfig copy() {
        return new PachinkoConfig(this);
    }
}
