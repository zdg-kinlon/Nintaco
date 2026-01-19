package nintaco.input.other;

import nintaco.App;
import nintaco.Machine;
import nintaco.input.OtherInput;
import nintaco.util.EDT;

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
        EDT.async(() -> App.getImageFrame().getNsfPanel()
                .fireTrackChanged(songNumber));
    }
}
