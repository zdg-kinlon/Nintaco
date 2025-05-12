package cn.kinlon.emu.utils;

import java.util.*;
import java.util.List;

public final class ThreadUtil {

    private ThreadUtil() {
    }

    public static void threadJoin(final Thread thread) {
        try {
            if (thread != Thread.currentThread()) {
                thread.join();
            }
        } catch (Throwable t) {
            //t.printStackTrace();
        }
    }

    // The caller must synchronize on obj to avoid an internally-caught exception.
    public static void threadWait(final Object obj) {
        try {
            obj.wait();
        } catch (Throwable t) {
        }
    }

    // The caller must synchronize on obj to avoid an internally-caught exception.
    public static void threadWait(final Object obj, final long timeout) {
        if (timeout > 0) {
            try {
                obj.wait(timeout);
            } catch (Throwable t) {
            }
        }
    }

    public static void sleep(final long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (final Throwable t) {
                //t.printStackTrace();
            }
        }
    }

    public static void join(final Thread thread) {
        if (thread != null && !Thread.currentThread().equals(thread)) {
            try {
                thread.join();
            } catch (final Throwable t) {
            }
        }
    }

    public static void joinAll(final Thread... threads) {
        for (final Thread thread : threads) {
            join(thread);
        }
    }

    public static void joinAll(final List<Thread> threads) {
        final List<Thread> ts;
        synchronized (threads) {
            ts = new ArrayList<>(threads);
        }
        for (final Thread thread : ts) {
            join(thread);
        }
    }

    // https://bugs.java.com/view_bug.do?bug_id=6435126
    public static void forceHighResolutionTime() {
        new Thread() {
            {
                this.setDaemon(true);
                this.start();
            }

            @Override
            public void run() {
                while (true) {
                    ThreadUtil.sleep(Integer.MAX_VALUE);
                }
            }
        };
    }
}
