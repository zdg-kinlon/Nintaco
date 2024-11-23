package nintaco.input.konamihypershot;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.KonamiHyperShot;

public class KonamiHyperShotConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public KonamiHyperShotConfig() {
        super(KonamiHyperShot);
    }

    public KonamiHyperShotConfig(final List<ButtonMapping> buttonMappings) {
        super(KonamiHyperShot, buttonMappings);
    }

    public KonamiHyperShotConfig(
            final KonamiHyperShotConfig konamiHyperShotConfig) {
        super(konamiHyperShotConfig);
    }

    @Override
    public KonamiHyperShotConfig copy() {
        return new KonamiHyperShotConfig(this);
    }
}
