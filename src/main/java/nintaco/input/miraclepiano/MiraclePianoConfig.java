package nintaco.input.miraclepiano;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.MiraclePiano;

public class MiraclePianoConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public MiraclePianoConfig() {
        super(MiraclePiano);
    }

    public MiraclePianoConfig(final List<ButtonMapping> buttonMappings) {
        super(MiraclePiano, buttonMappings);
    }

    public MiraclePianoConfig(final MiraclePianoConfig miraclePianoConfig) {
        super(miraclePianoConfig);
    }

    @Override
    public MiraclePianoConfig copy() {
        return new MiraclePianoConfig(this);
    }
}