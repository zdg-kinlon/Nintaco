package nintaco.api.server;

import static nintaco.util.ThreadUtil.threadWait;

public class ListenerLocker {

    private boolean request;
    private boolean response;
    private boolean generatesResult;
    private boolean result;
    private boolean disposed;

    public synchronized void requestReceived() {
        request = true;
        notifyAll();
    }

    public synchronized void waitForRequest(final boolean generatesResult) {

        while (!(request || disposed)) {
            threadWait(this);
        }

        if (disposed) {
            throw new RuntimeException("Disposed.");
        }

        request = false;
        this.generatesResult = generatesResult;
    }

    public synchronized void resultReceived() {
        result = true;
        notifyAll();
    }

    public synchronized void responseReceived() {
        response = true;
        notifyAll();

        if (generatesResult) {
            while (!(result || disposed)) {
                threadWait(this);
            }

            if (disposed) {
                throw new RuntimeException("Disposed.");
            }

            generatesResult = result = false;
        }
    }

    public synchronized void waitForResponse() {

        while (!(response || disposed)) {
            threadWait(this);
        }

        if (disposed) {
            throw new RuntimeException("Disposed.");
        }

        response = false;
    }

    public synchronized void dispose() {
        disposed = true;
        notifyAll();
    }
}
