package nintaco.input.bandaihypershot;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.BandaiHyperShot;

public class BandaiHyperShotConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public BandaiHyperShotConfig() {
        super(BandaiHyperShot);
    }

    public BandaiHyperShotConfig(final List<ButtonMapping> buttonMappings) {
        super(BandaiHyperShot, buttonMappings);
    }

    public BandaiHyperShotConfig(
            final BandaiHyperShotConfig bandaiHyperShotConfig) {
        super(bandaiHyperShotConfig);
    }

    @Override
    public BandaiHyperShotConfig copy() {
        return new BandaiHyperShotConfig(this);
    }
}
