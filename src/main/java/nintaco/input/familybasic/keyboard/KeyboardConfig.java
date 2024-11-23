package nintaco.input.familybasic.keyboard;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Keyboard;

public class KeyboardConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public KeyboardConfig() {
        super(Keyboard);
    }

    public KeyboardConfig(final List<ButtonMapping> buttonMappings) {
        super(Keyboard, buttonMappings);
    }

    public KeyboardConfig(final KeyboardConfig keyboardConfig) {
        super(keyboardConfig);
    }

    @Override
    public KeyboardConfig copy() {
        return new KeyboardConfig(this);
    }
}
