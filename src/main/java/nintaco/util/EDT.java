package nintaco.util;

import nintaco.MessageException;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EDT {
    private static final Runnable EMPTY_RUN = () -> {
    };

    public static void sync(Runnable run) {
        run = Optional.ofNullable(run).orElse(EMPTY_RUN);
        if (EventQueue.isDispatchThread()) {
            run.run();
            return;
        }
        try {
            EventQueue.invokeAndWait(run);
        } catch (InterruptedException e) {
            throw new MessageException("EDT interrupted. ", e);
        } catch (InvocationTargetException e) {
            throw new MessageException("EDT failed. ", e);
        }
    }

    public static void async(Runnable run) {
        run = Optional.ofNullable(run).orElse(EMPTY_RUN);
        if (EventQueue.isDispatchThread()) {
            run.run();
            return;
        }
        EventQueue.invokeLater(run);
    }

    public static <T> T sync(final Supplier<T> invocation) {
        final Reference<T> ref = new Reference<>();
        EDT.sync(() -> ref.set(invocation.get()));
        return ref.get();
    }

    private EDT() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
