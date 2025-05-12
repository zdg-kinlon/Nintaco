package cn.kinlon.emu.input.racermate;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.RacerMate2;

public class RacerMate2Config extends RacerMateConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public RacerMate2Config() {
        super(RacerMate2);
    }

    public RacerMate2Config(final RacerMate2Config racerMate2Config) {
        super(racerMate2Config);
    }

    @Override
    public RacerMate2Config copy() {
        return new RacerMate2Config(this);
    }
}