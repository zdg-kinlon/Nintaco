package cn.kinlon.emu.utils;

import java.io.*;
import java.nio.channels.*;

import static cn.kinlon.emu.files.FileUtil.*;

public final class InstanceUtil {

    private static File file;
    private static FileChannel fileChannel;
    private static FileLock lock;

    public static boolean isAlreadyRunning() {

        file = new File(appendSeparator(System.getProperty("java.io.tmpdir"))
                + "nintaco.lock");
        try {
            fileChannel = new RandomAccessFile(file, "rw").getChannel();
            lock = fileChannel.tryLock();
        } catch (final Throwable t) {
            return false;
        }

        if (lock == null) {
            try {
                fileChannel.close();
            } catch (final Throwable t) {
            }
            return true;
        }
        Runtime.getRuntime().addShutdownHook(
                new Thread(InstanceUtil::unlockFile));

        return false;
    }

    private static void unlockFile() {
        if (lock != null) {
            try {
                lock.release();
            } catch (final Throwable t) {
            }
        }
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (final Throwable t) {
            }
        }
        if (file != null) {
            try {
                file.delete();
            } catch (final Throwable t) {
            }
        }
    }

    private InstanceUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
