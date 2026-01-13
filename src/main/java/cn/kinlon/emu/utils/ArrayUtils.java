package cn.kinlon.emu.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;

public final class ArrayUtils {

    public static final byte[] EMPTY_ARRAY_I8 = new byte[0];
    public static final int[] EMPTY_ARRAY_U8 = new int[0];

    public static boolean isEmpty(final Object o) {
        return o == null || Array.getLength(o) == 0;
    }

    public static int[] concat(int[] a, int[] b) {
        if (a == null) a = EMPTY_ARRAY_U8;
        if (b == null) b = EMPTY_ARRAY_U8;
        return IntStream.concat(Arrays.stream(a), Arrays.stream(b)).toArray();
    }

    public static int[] concatCopy(int[] a, int newLength) {
        int[] r = EMPTY_ARRAY_U8;
        while (r.length < newLength) {
            r = concat(a, Arrays.copyOfRange(a, 0, Math.min(r.length, newLength - r.length)));
        }
        return r;
    }

    private ArrayUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
