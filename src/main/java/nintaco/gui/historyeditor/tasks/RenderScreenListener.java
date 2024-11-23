package nintaco.gui.historyeditor.tasks;

import nintaco.MachineRunner;
import nintaco.movie.MovieFrame;

public interface RenderScreenListener {
    void completedRendering(final MachineRunner machineRunner,
                            MovieFrame movieFrame);
}
