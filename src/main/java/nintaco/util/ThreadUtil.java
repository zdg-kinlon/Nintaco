package nintaco.util;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.lang.reflect.*;

public final class ThreadUtil {

    private static class TargetInvoker implements Runnable {

        private final Object target;
        private final Method method;
        private final Object[] arguments;

        private Object returnValue;
        private Throwable exception;

        public TargetInvoker(Object target, Method method,
                             Object[] arguments) {
            this.target = target;
            this.method = method;
            this.arguments = arguments;
        }

        public boolean threwException() {
            return exception != null;
        }

        public Throwable getException() {
            return exception;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void run() {
            try {
                returnValue = method.invoke(target, arguments);
            } catch (Throwable t) {
                exception = t;
            }
        }
    }

    private static class WorkerHandler implements InvocationHandler {

        private final Class targetClass;
        private final Object target;

        public WorkerHandler(Object target) {
            this.target = target;
            this.targetClass = target.getClass();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            new Thread(new TargetInvoker(target, targetClass.getMethod(method.getName(), method.getParameterTypes()), args)).start();
            return null;
        }
    }

    private static class SwingHandler implements InvocationHandler {

        private final Class targetClass;
        private final Object target;

        public SwingHandler(Object target) {
            this.target = target;
            this.targetClass = target.getClass();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            final TargetInvoker targetInvoker = new TargetInvoker(
                    target, targetClass.getMethod(method.getName(),
                    method.getParameterTypes()), args);
            if (method.getReturnType() == void.class) {
                EDT.async(targetInvoker);
            } else {
                EDT.sync(targetInvoker);
                if (targetInvoker.threwException()) {
                    throw targetInvoker.getException();
                } else {
                    return targetInvoker.getReturnValue();
                }
            }
            return null;
        }
    }

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

    public static void pause() {
        sleep(1000L);
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

    public static Object createWorkerProxy(final Object target) {
        return Proxy.newProxyInstance(ThreadUtil.class.getClassLoader(),
                target.getClass().getInterfaces(), new WorkerHandler(target));
    }

    public static Object createSwingProxy(final Object target) {
        return Proxy.newProxyInstance(ThreadUtil.class.getClassLoader(),
                target.getClass().getInterfaces(), new SwingHandler(target));
    }

    public static void interrupt(final Thread thread) {
        if (thread != null) {
            try {
                thread.interrupt();
            } catch (final Throwable t) {
            }
        }
    }

    public static void interruptAll(final Thread... threads) {
        for (final Thread thread : threads) {
            interrupt(thread);
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
