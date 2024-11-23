package nintaco.util;

public final class MathUtil {

    public static final Integer ZERO = 0;

    private static final int[] digitsTable = {9, 99, 999, 9999, 99999, 999999,
            9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private MathUtil() {
    }

    public static final int getDigits(final int value) {
        for (int i = 0; ; i++) {
            if (value <= digitsTable[i]) {
                return i + 1;
            }
        }
    }

    public static final int clamp(final int value, final int min, final int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public static final double clamp(final double value, final double min,
                                     final double max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public static final float clamp(final float value, final float min,
                                    final float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public static final int roundUp(final int value, final int unit) {
        if (value % unit == 0) {
            return value;
        } else {
            return ((value / unit) + 1) * unit;
        }
    }

    public static final double deviation(final double idealValue,
                                         final double measuredValue) {
        return Math.abs(idealValue - measuredValue) / idealValue;
    }

    public static final boolean isOdd(final long value) {
        return (value & 1) == 1;
    }

    public static final boolean isEven(final long value) {
        return (value & 1) == 0;
    }

    public static final boolean isOdd(final int value) {
        return (value & 1) == 1;
    }

    public static final boolean isEven(final int value) {
        return (value & 1) == 0;
    }

    // m/n 
    public static final int roundUpDivision(final int m, final int n) {
        return (m + n - 1) / n;
    }

    public static final int hash(byte[] data) {
        return hash(data, 0, data.length);
    }

    public static final int hash(final byte[] data, final int offset,
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

    public static final int hash(final byte[][] data, final int x, final int y,
                                 final int width, final int height) {
        int hash = width * height;
        for (int i = height - 1; i >= 0; i--) {
            final byte[] row = data[i + y];
            for (int j = width - 1; j >= 0; j--) {
                hash += row[j + x] & 0xFF;
                hash += hash << 10;
                hash ^= hash >> 6;
            }
        }
        hash += hash << 3;
        hash ^= hash >> 11;
        hash += hash << 15;
        return hash;
    }
}
