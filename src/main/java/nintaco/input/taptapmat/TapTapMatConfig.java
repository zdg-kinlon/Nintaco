package nintaco.input.taptapmat;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.TapTapMat;

public class TapTapMatConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public TapTapMatConfig() {
        super(TapTapMat);
    }

    public TapTapMatConfig(final List<ButtonMapping> buttonMappings) {
        super(TapTapMat, buttonMappings);
    }

    public TapTapMatConfig(final TapTapMatConfig tapTapMatConfig) {
        super(tapTapMatConfig);
    }

    @Override
    public TapTapMatConfig copy() {
        return new TapTapMatConfig(this);
    }
}
