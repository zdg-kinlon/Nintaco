package nintaco.input.gamepad;

import nintaco.input.ButtonMapping;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Gamepad3;

public class Gamepad3Config extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public Gamepad3Config() {
        super(Gamepad3);
    }

    public Gamepad3Config(final List<ButtonMapping> buttonMappings) {
        super(Gamepad3, buttonMappings);
    }

    public Gamepad3Config(final Gamepad3Config gamepad3Config) {
        super(gamepad3Config);
    }

    @Override
    public Gamepad3Config copy() {
        return new Gamepad3Config(this);
    }
}
