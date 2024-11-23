package nintaco.input.crazyclimber;

import nintaco.input.ButtonMapping;
import nintaco.input.gamepad.GamepadConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.CrazyClimberRight;

public class CrazyClimberRightConfig
        extends GamepadConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public CrazyClimberRightConfig() {
        super(CrazyClimberRight);
    }

    public CrazyClimberRightConfig(final List<ButtonMapping> buttonMappings) {
        super(CrazyClimberRight, buttonMappings);
    }

    public CrazyClimberRightConfig(
            final CrazyClimberRightConfig crazyClimberRightConfig) {
        super(crazyClimberRightConfig);
    }

    @Override
    public CrazyClimberRightConfig copy() {
        return new CrazyClimberRightConfig(this);
    }
}
