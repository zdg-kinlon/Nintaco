package cn.kinlon.emu.input.racermate;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.RacerMate1;

public class RacerMate1Config extends RacerMateConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public RacerMate1Config() {
        super(RacerMate1);
    }

    public RacerMate1Config(final RacerMate1Config racerMate1Config) {
        super(racerMate1Config);
    }

    @Override
    public RacerMate1Config copy() {
        return new RacerMate1Config(this);
    }
}