package cn.kinlon.emu.utils;

public final class MathUtil {

    private MathUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static int clamp(final int value, final int min, final int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public static double clamp(final double value, final double min,
                               final double max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public static int roundUp(final int value, final int unit) {
        if (value % unit == 0) {
            return value;
        } else {
            return ((value / unit) + 1) * unit;
        }
    }

    public static boolean isOdd(final long value) {
        return (value & 1) == 1;
    }

    public static boolean isOdd(final int value) {
        return (value & 1) == 1;
    }

    public static boolean isEven(final int value) {
        return (value & 1) == 0;
    }

    // m/n 
    public static int roundUpDivision(final int m, final int n) {
        return (m + n - 1) / n;
    }

}
