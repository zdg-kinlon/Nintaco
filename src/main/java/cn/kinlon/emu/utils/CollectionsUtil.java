package cn.kinlon.emu.utils;

import java.lang.reflect.*;
import java.util.*;

public final class CollectionsUtil {

    private CollectionsUtil() {
    }

    public static boolean isBlank(final Object array) {
        return array == null || Array.getLength(array) == 0;
    }

    public static boolean isBlank(final List list) {
        return list == null || list.isEmpty();
    }

    public static <T extends Comparable<T>> int compare(final T a, final T b) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else {
            return a.compareTo(b);
        }
    }

    public static int[] toIntArray(final byte[] values) {
        if (values == null) {
            return null;
        }
        final int[] vs = new int[values.length];
        for (int i = vs.length - 1; i >= 0; i--) {
            vs[i] = values[i] & 0xFF;
        }
        return vs;
    }

    public static <T> T[] convertToArray(final Class<T> c, final List<T> list) {
        return convertToArray(c, list, true);
    }

    public static <T> T[] convertToArray(final Class<T> c, final List<T> list,
                                         final boolean returnNullForEmptyList) {
        if (list == null || list.isEmpty()) {
            if (returnNullForEmptyList) {
                return null;
            } else {
                return (T[]) Array.newInstance(c, 0);
            }
        } else {
            final T[] ts = (T[]) Array.newInstance(c, list.size());
            list.toArray(ts);
            return ts;
        }
    }

    public static <T> T[] addElement(final Class<T> c, final T[] array,
                                     final T element) {

        final T[] a;
        if (array == null) {
            a = (T[]) Array.newInstance(c, 1);
            a[0] = element;
        } else {
            for (int i = array.length - 1; i >= 0; i--) {
                if (Objects.equals(array[i], element)) {
                    return array;
                }
            }
            a = (T[]) Array.newInstance(c, array.length + 1);
            System.arraycopy(array, 0, a, 0, array.length);
            a[a.length - 1] = element;
        }
        return a;
    }

    public static <T> T[] removeElement(final Class<T> c, final T[] array,
                                        final int index) {

        if (array == null) {
            return null;
        }
        if (index >= array.length || index < 0) {
            return array;
        }
        if (array.length <= 1) {
            return null;
        }
        final T[] a = (T[]) Array.newInstance(c, array.length - 1);
        if (index != 0) {
            System.arraycopy(array, 0, a, 0, index);
        }
        if (index != array.length - 1) {
            System.arraycopy(array, index + 1, a, index, array.length - index - 1);
        }
        return a;
    }

    public static <T> boolean compareArrays(final T[] a, final T[] b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null || a.length != b.length) {
            return false;
        } else {
            for (int i = a.length - 1; i >= 0; i--) {
                if (!Objects.equals(a[i], b[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}
