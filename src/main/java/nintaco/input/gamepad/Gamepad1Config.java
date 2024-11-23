package nintaco.input.gamepad;

import nintaco.input.ButtonMapping;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.Gamepad1;

public class Gamepad1Config extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public Gamepad1Config() {
        super(Gamepad1);
    }

    public Gamepad1Config(final List<ButtonMapping> buttonMappings) {
        super(Gamepad1, buttonMappings);
    }

    public Gamepad1Config(final Gamepad1Config gamepad1Config) {
        super(gamepad1Config);
    }

    @Override
    public Gamepad1Config copy() {
        return new Gamepad1Config(this);
    }
}
