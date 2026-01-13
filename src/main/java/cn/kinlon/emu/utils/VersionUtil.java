package cn.kinlon.emu.utils;

public final class VersionUtil {

    private VersionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static String getVersion() {
        return "2024.10.27";
    }
}