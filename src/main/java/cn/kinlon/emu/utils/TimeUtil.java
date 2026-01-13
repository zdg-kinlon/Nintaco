package cn.kinlon.emu.utils;


import cn.kinlon.emu.App;
import cn.kinlon.emu.gui.userinterface.InterframeDelay;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.tv.TVSystem;

import java.util.concurrent.locks.LockSupport;

import static cn.kinlon.emu.tv.TVSystem.*;

public class TimeUtil {

    private static volatile int speedPercent = 100;
    private static volatile TVSystem tvSystem = NTSC;
    private static volatile long nanosPerFrame;
    private static volatile boolean diskActivity;
    private static volatile boolean highSpeed;
    private static volatile int maxLagFrames;
    private static volatile DelayStrategy delayStrategy;
    private static volatile long frameLostThreshold;

    static {
        updateSpeed();
        setMaxLagFrames(60); // TODO ENHANCE
    }

    private static void updateSpeed() {
        final AppPrefs prefs = AppPrefs.getInstance();
        final int percent = highSpeed ? prefs.getInputs().getHighSpeedRate()
                : speedPercent;
        if (percent == 0 || (diskActivity && prefs.getFamicomDiskSystemPrefs()
                .isFastForwardDuringDiskAccess())) {
            nanosPerFrame = 0;
        } else {
            nanosPerFrame = tvSystem.nanosPerFrame() * 100L / percent;
        }
        updateFrameLostThreshold();
    }

    public static boolean isHighSpeed() {
        return highSpeed;
    }

    public static void setHighSpeed(final boolean highSpeed) {
        TimeUtil.highSpeed = highSpeed;
        updateSpeed();
    }

    public static int getSpeed() {
        return speedPercent;
    }

    public static void setSpeed(final int speedPercent) {
        TimeUtil.speedPercent = speedPercent;
        updateSpeed();
    }

    public static void setMaxLagFrames(int maxLagFrames) {
        TimeUtil.maxLagFrames = maxLagFrames;
        updateFrameLostThreshold();
    }

    public static void setInterframeDelay(final InterframeDelay interframeDelay) {
        switch (interframeDelay) {
            case Sleep -> delayStrategy = (nanoTime) -> {
                LockSupport.parkNanos(nanoTime - System.nanoTime());
            };
            case Yield -> delayStrategy = (nanoTime) -> {
                while (nanoTime - System.nanoTime() > 0) {
                    Thread.yield();
                }
            };
            case Spin -> delayStrategy = (nanoTime) -> {
                while (nanoTime - System.nanoTime() > 0) {
                    Thread.onSpinWait();
                }
            };
        }
    }

    private static void updateFrameLostThreshold() {
        frameLostThreshold = maxLagFrames * nanosPerFrame;
    }

    public static long sleep(long next, final Mapper mapper) {
        final TVSystem system;
        if (mapper != null) {
            final boolean activity = mapper.isDiskActivity();
            if (TimeUtil.diskActivity != activity) {
                TimeUtil.diskActivity = activity;
                updateSpeed();
            }
            App.setDiskActivity(activity);
            system = mapper.getTVSystem();
        } else {
            system = NTSC;
        }
        if (TimeUtil.tvSystem != system) {
            TimeUtil.tvSystem = system;
            updateSpeed();
        }
        if (nanosPerFrame > 0) {
            next += nanosPerFrame;
            if (System.nanoTime() - next > frameLostThreshold) {
                next = System.nanoTime(); // frame lost
            } else {
                delayStrategy.delay(next);
            }
        } else {
            next = System.nanoTime();
        }
        return next;
    }

    @FunctionalInterface
    public interface DelayStrategy {
        void delay(long nano);
    }
}
