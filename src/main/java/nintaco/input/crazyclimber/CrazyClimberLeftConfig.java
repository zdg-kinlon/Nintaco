package nintaco.input.crazyclimber;

import nintaco.input.ButtonMapping;
import nintaco.input.gamepad.GamepadConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.CrazyClimberLeft;

public class CrazyClimberLeftConfig
        extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public CrazyClimberLeftConfig() {
        super(CrazyClimberLeft);
    }

    public CrazyClimberLeftConfig(final List<ButtonMapping> buttonMappings) {
        super(CrazyClimberLeft, buttonMappings);
    }

    public CrazyClimberLeftConfig(
            final CrazyClimberLeftConfig crazyClimberLeftConfig) {
        super(crazyClimberLeftConfig);
    }

    @Override
    public CrazyClimberLeftConfig copy() {
        return new CrazyClimberLeftConfig(this);
    }
}
