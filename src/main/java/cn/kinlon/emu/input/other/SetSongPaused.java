package cn.kinlon.emu.input.other;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;
import cn.kinlon.emu.utils.EDT;

import java.awt.*;

public class SetSongPaused implements OtherInput {

    private static final long serialVersionUID = 0;

    private final boolean paused;

    public SetSongPaused(final boolean paused) {
        this.paused = paused;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().setSongPaused(paused);
        EDT.async(() -> App.getImageFrame().getNsfPanel().updateClock());
    }
}
