package cn.kinlon.emu.utils;

public final class ThreadUtils {

    // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4500388
    public static void enableHighResolutionTimer() {
        Thread.ofPlatform()
                .name("force high resolution time")
                .daemon(true)
                .priority(Thread.MIN_PRIORITY)
                .start(() -> {
                    try {
                        // 用于在 Windows 上保持高精度定时器，
                        // 避免反复切换带来的开销或时钟漂移
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
    }

    public static Thread async_io(Runnable run) {
        return Thread.ofVirtual().start(run);
    }

    public static Thread async_calc(Runnable run) {
        return Thread.ofPlatform().start(run);
    }

    private ThreadUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
