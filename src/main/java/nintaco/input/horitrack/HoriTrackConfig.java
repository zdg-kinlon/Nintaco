package nintaco.input.horitrack;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.HoriTrack;

public class HoriTrackConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public HoriTrackConfig() {
        super(HoriTrack);
    }

    public HoriTrackConfig(final List<ButtonMapping> buttonMappings) {
        super(HoriTrack, buttonMappings);
    }

    public HoriTrackConfig(final HoriTrackConfig horiTrackConfig) {
        super(horiTrackConfig);
    }

    @Override
    public HoriTrackConfig copy() {
        return new HoriTrackConfig(this);
    }
}