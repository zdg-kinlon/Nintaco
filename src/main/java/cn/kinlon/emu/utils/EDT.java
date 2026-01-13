package cn.kinlon.emu.utils;

import cn.kinlon.emu.MessageException;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class EDT {

    private static final Runnable EMPTY_RUN = () -> {
    };

    public static void sync(Runnable run) {
        if (run == null) {
            run = EMPTY_RUN;
        }
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
        if (run == null) {
            run = EMPTY_RUN;
        }
        if (EventQueue.isDispatchThread()) {
            run.run();
            return;
        }
        EventQueue.invokeLater(run);
    }

    private EDT() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
