package cn.kinlon.emu.input.other;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;
import cn.kinlon.emu.utils.EDT;

import java.awt.*;

public class RequestSong implements OtherInput {

    private static final long serialVersionUID = 0;

    private final int songNumber;

    public RequestSong(final int songNumber) {
        this.songNumber = songNumber;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().requestSong(songNumber);
        EDT.async(() -> App.getImageFrame().getNsfPanel().fireTrackChanged(songNumber));
    }
}
