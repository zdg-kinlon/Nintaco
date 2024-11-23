package nintaco.input.topriderbike;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.TopRiderBike;

public class TopRiderBikeConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TopRiderBikeConfig() {
        super(TopRiderBike);
    }

    public TopRiderBikeConfig(final List<ButtonMapping> buttonMappings) {
        super(TopRiderBike, buttonMappings);
    }

    public TopRiderBikeConfig(final TopRiderBikeConfig topRiderBikeConfig) {
        super(topRiderBikeConfig);
    }

    @Override
    public TopRiderBikeConfig copy() {
        return new TopRiderBikeConfig(this);
    }
}
