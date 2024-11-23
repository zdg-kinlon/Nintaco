package nintaco.netplay.queue;

import java.io.*;

import static nintaco.netplay.queue.ElementDataType.*;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.ThreadUtil.*;

public class RingQueue {

    private static final int SIZE = 0x100;
    private static final int MASK = SIZE - 1;

    private final Object monitor = new Object();
    private final QueueElement[] elements = new QueueElement[SIZE];

    private volatile int head;
    private volatile boolean running;

    public RingQueue() {
        for (int i = elements.length - 1; i >= 0; i--) {
            elements[i] = new QueueElement();
        }
    }

    public void produce(final int messageType) {
        produce(messageType, NONE, 0, (byte[]) null);
    }

    public void produce(final int messageType, final int value) {
        produce(messageType, INTEGER, value, (byte[]) null);
    }

    public void produce(final int messageType, final Serializable serializable) {
        produce(messageType, BYTES, 0, serializable == null ? null
                : toByteArrayOutputStream(serializable).toByteArray());
    }

    public void produce(final int messageType, final int value,
                        final Serializable serializable) {
        produce(messageType, ALL, value, serializable == null ? null
                : toByteArrayOutputStream(serializable).toByteArray());
    }

    public void produce(final int messageType, final ByteArrayOutputStream data) {
        produce(messageType, BYTES, 0, data == null ? null : data.toByteArray());
    }

    public void produce(final int messageType, final int value,
                        final ByteArrayOutputStream data) {
        produce(messageType, ALL, value, data == null ? null : data.toByteArray());
    }

    public void produce(final int messageType, final byte[] data) {
        produce(messageType, BYTES, 0, data);
    }

    public void produce(final int messageType, final int value,
                        final byte[] data) {
        produce(messageType, ALL, value, data);
    }

    private void produce(final int messageType, final ElementDataType dataType,
                         final int value, final byte[] data) {
        synchronized (monitor) {
            if (!running) {
                return;
            }
            final QueueElement element = elements[head & MASK];
            element.messageType = messageType;
            element.dataType = dataType;
            element.value = value;
            element.data = data;
            head++;
            monitor.notifyAll();
        }
    }

    public void start() {
        synchronized (monitor) {
            head = 0;
            running = true;
            monitor.notifyAll();
        }
    }

    public void stop() {
        synchronized (monitor) {
            running = false;
            monitor.notifyAll();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getHead() {
        return head;
    }

    public boolean consume(final int tail, final QueueElement element) {
        if (!running) {
            return false;
        }
        if (tail >= head) {
            synchronized (monitor) {
                while (running && tail == head) {
                    threadWait(monitor);
                }
            }
        }
        if (!running || head - tail >= SIZE) {
            return false;
        }
        final QueueElement e = elements[tail & MASK];
        element.data = e.data;
        element.dataType = e.dataType;
        element.messageType = e.messageType;
        element.value = e.value;
        return true;
    }
}
