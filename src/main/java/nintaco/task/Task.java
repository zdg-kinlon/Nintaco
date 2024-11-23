package nintaco.task;

public abstract class Task implements Runnable {

    protected volatile boolean running;
    protected volatile boolean canceled;

    @Override
    public void run() {
        synchronized (this) {
            if (canceled) {
                return;
            }
            running = true;
        }
        try {
            loop();
        } finally {
            running = false;
        }
    }

    public abstract void loop();

    public synchronized void cancel() {
        running = false;
        canceled = true;
        notifyAll();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isRunning() {
        return running;
    }
}
