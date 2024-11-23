package nintaco.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import static java.lang.Math.*;

public final class CollectionsUtil {

    private CollectionsUtil() {
    }

    public static boolean isBlank(final Object array) {
        return array == null || Array.getLength(array) == 0;
    }

    public static boolean isBlank(final List list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean contains(final T[] a, final T element) {
        for (int i = a.length - 1; i >= 0; i--) {
            if (Objects.equals(a[i], element)) {
                return true;
            }
        }
        return false;
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

    public static int[] toIntArray(final Integer[] values) {
        if (values == null) {
            return null;
        }
        final int[] vs = new int[values.length];
        for (int i = vs.length - 1; i >= 0; i--) {
            final Integer value = values[i];
            vs[i] = value == null ? 0 : value;
        }
        return vs;
    }

    public static Integer[] toIntegerArray(final int[] values) {
        if (values == null) {
            return null;
        }
        final Integer[] vs = new Integer[values.length];
        for (int i = vs.length - 1; i >= 0; i--) {
            vs[i] = values[i];
        }
        return vs;
    }

    public static <T> T[] convertToArray(final List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return convertToArray((Class<T>) list.get(0).getClass(), list);
        }
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

    public static <T> T[] resize(final Class<T> c, T[] a, final int length) {
        final T[] b = (T[]) Array.newInstance(c, length);
        if (a != null) {
            System.arraycopy(a, 0, b, 0, min(a.length, b.length));
        }
        return b;
    }

    public static <T> T[] concat(final Class<T> c, T[] a, T[] b) {
        if (a != null && a.length == 0) {
            a = null;
        }
        if (b != null && b.length == 0) {
            b = null;
        }
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        final T[] array = (T[]) Array.newInstance(c, a.length + b.length);
        System.arraycopy(a, 0, array, 0, a.length);
        System.arraycopy(b, 0, array, a.length, b.length);
        return array;
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

    public static <T> T[] removeAllElements(final Class<T> c, final T[] array,
                                            final T element) {

        if (array == null) {
            return null;
        }

        T[] result = array;
        for (int i = result.length - 1; i >= 0; i--) {
            if (Objects.equals(result[i], element)) {
                result = removeElement(c, result, i);
            }
        }
        return result;
    }

    public static <T> T[] removeAll(final Class<T> c, final T[] array,
                                    final Predicate<T> predicate) {

        if (array == null) {
            return null;
        }

        T[] result = array;
        for (int i = result.length - 1; i >= 0; i--) {
            if (predicate.test(result[i])) {
                result = removeElement(c, result, i);
            }
        }
        return result;
    }

    public static <T> T[] removeElement(final Class<T> c, final T[] array,
                                        final T element) {

        if (array == null) {
            return null;
        }

        final int index;
        outer:
        {
            for (int i = array.length - 1; i >= 0; i--) {
                if (Objects.equals(array[i], element)) {
                    index = i;
                    break outer;
                }
            }
            return array;
        }

        return removeElement(c, array, index);
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

    public static boolean compareArrays(final byte[] a, final byte[] b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null || a.length != b.length) {
            return false;
        } else {
            for (int i = a.length - 1; i >= 0; i--) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
        }
        return true;
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
