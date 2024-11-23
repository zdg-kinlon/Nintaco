package nintaco.gui.historyeditor.tasks;

import nintaco.task.Task;

@FunctionalInterface
public interface TaskTerminatedListener {
    void taskTerminated(Task task);
}
