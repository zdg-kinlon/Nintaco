package nintaco.gui.historyeditor.tasks;

import nintaco.MachineRunner;
import nintaco.task.Task;

@FunctionalInterface
public interface FramePlayedListener {
    void framePlayed(Task task, int frameIndex, MachineRunner machineRunner);
}
