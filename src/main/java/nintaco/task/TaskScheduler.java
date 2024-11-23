package nintaco.task;

import java.util.*;

import static nintaco.util.ThreadUtil.*;

public class TaskScheduler {

    private final List<Task> tasks = new ArrayList<>();
    private final Thread thread;

    private volatile boolean running = true;

    private boolean ready;

    public TaskScheduler() {
        thread = new Thread(this::run, "Task Secheduler Thread");
        thread.start();
    }

    private void run() {
        while (running) {
            Task task = null;
            synchronized (tasks) {
                ready = true;
                tasks.notifyAll();
                while (running && tasks.isEmpty()) {
                    threadWait(tasks);
                }
                ready = false;
                tasks.notifyAll();
                if (running && !tasks.isEmpty()) {
                    task = tasks.get(0);
                }
            }
            if (running && task != null) {
                try {
                    task.run();
                } catch (final Throwable t) {
                    //t.printStackTrace();
                }
                try {
                    Thread.interrupted();
                } catch (final Throwable t) {
                }
                synchronized (tasks) {
                    tasks.remove(0);
                }
            }
        }
    }

    public void cancelAll() {
        synchronized (tasks) {
            for (final Task task : tasks) {
                task.cancel();
            }
            tasks.notifyAll();
        }
    }

    public void waitForEmpty() {
        synchronized (tasks) {
            while (running && !tasks.isEmpty()) {
                threadWait(tasks);
            }
        }
    }

    public void waitForReady() {
        synchronized (tasks) {
            while (running && !ready) {
                threadWait(tasks);
            }
        }
    }

    public void add(final Task task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    public void dispose() {
        running = false;
        cancelAll();
        threadJoin(thread);
    }
}
