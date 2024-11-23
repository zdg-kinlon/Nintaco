package nintaco.input.doremikkokeyboard;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.DoremikkoKeyboard;

public class DoremikkoKeyboardConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public DoremikkoKeyboardConfig() {
        super(DoremikkoKeyboard);
    }

    public DoremikkoKeyboardConfig(final List<ButtonMapping> buttonMappings) {
        super(DoremikkoKeyboard, buttonMappings);
    }

    public DoremikkoKeyboardConfig(
            final DoremikkoKeyboardConfig doremikkoKeyboardConfig) {
        super(doremikkoKeyboardConfig);
    }

    @Override
    public DoremikkoKeyboardConfig copy() {
        return new DoremikkoKeyboardConfig(this);
    }
}