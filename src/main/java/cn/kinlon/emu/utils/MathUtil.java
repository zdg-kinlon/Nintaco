package cn.kinlon.emu.utils;

public final class MathUtil {

    private static final int[] digitsTable = {9, 99, 999, 9999, 99999, 999999,
            9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private MathUtil() {
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

    public static int hash(final byte[] data, final int offset,
                           final int length) {
        int hash = length;
        for (int i = length - 1; i >= 0; i--) {
            hash += data[i + offset] & 0xFF;
            hash += hash << 10;
            hash ^= hash >> 6;
        }
        hash += hash << 3;
        hash ^= hash >> 11;
        hash += hash << 15;
        return hash;
    }

}
