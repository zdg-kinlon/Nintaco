package nintaco.input.partytap;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.PartyTap;

public class PartyTapConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public PartyTapConfig() {
        super(PartyTap);
    }

    public PartyTapConfig(final List<ButtonMapping> buttonMappings) {
        super(PartyTap, buttonMappings);
    }

    public PartyTapConfig(
            final PartyTapConfig partyTapConfig) {
        super(partyTapConfig);
    }

    @Override
    public PartyTapConfig copy() {
        return new PartyTapConfig(this);
    }
}