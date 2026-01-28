package nintaco.input.other;

import nintaco.App;
import nintaco.Machine;
import nintaco.input.OtherInput;
import nintaco.util.EDT;

public class SetSongPaused implements OtherInput {

    private static final long serialVersionUID = 0;

    private final boolean paused;

    public SetSongPaused(final boolean paused) {
        this.paused = paused;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().setSongPaused(paused);
        EDT.async(() -> App.getImageFrame().getNsfPanel()
                .updateClock());
    }
}
