package nintaco.input.turbofile;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.TurboFile;

public class TurboFileConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TurboFileConfig() {
        super(TurboFile);
    }

    public TurboFileConfig(final List<ButtonMapping> buttonMappings) {
        super(TurboFile, buttonMappings);
    }

    public TurboFileConfig(final TurboFileConfig oekaKidsConfig) {
        super(oekaKidsConfig);
    }

    @Override
    public TurboFileConfig copy() {
        return new TurboFileConfig(this);
    }
}
