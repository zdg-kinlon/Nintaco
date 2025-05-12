package cn.kinlon.emu.input.other;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;

public class SetNsfOptions implements OtherInput {

    private static final long serialVersionUID = 0;

    private final boolean automaticallyAdvanceTrack;
    private final int idleSeconds;
    private final boolean defaultTrackLength;
    private final int trackLengthMinutes;

    public SetNsfOptions(final boolean automaticallyAdvanceTrack,
                         final int idleSeconds, final boolean defaultTrackLength,
                         final int trackLengthMinutes) {
        this.automaticallyAdvanceTrack = automaticallyAdvanceTrack;
        this.idleSeconds = idleSeconds;
        this.defaultTrackLength = defaultTrackLength;
        this.trackLengthMinutes = trackLengthMinutes;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().setNsfOptions(automaticallyAdvanceTrack, idleSeconds,
                defaultTrackLength, trackLengthMinutes);
    }
}
