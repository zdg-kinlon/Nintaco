package nintaco.util;

import java.text.*;
import java.util.*;

import nintaco.gui.*;

import static java.lang.Math.*;

public final class StringUtil {

    public static interface ParseErrors {
        int EMPTY = -1;
        int OUT_OF_RANGE = -2;
        int NOT_A_NUMBER = -3;
    }

    private static SimpleDateFormat hhmmssFormat
            = new SimpleDateFormat("HH:mm:ss");

    private StringUtil() {
    }

    public static String getTimeString() {
        return hhmmssFormat.format(new Date());
    }

    public static Integer parseInt(Object value, boolean hex, long minValue,
                                   long maxValue) {
        if (value == null) {
            return null;
        }
        String s = value.toString().trim();
        if (s.startsWith("$") || s.startsWith("x")) {
            s = s.substring(1);
            hex = true;
        } else if (s.startsWith("0x") || s.startsWith("&H") || s.startsWith("&h")) {
            s = s.substring(2);
            hex = true;
        }
        if (s.isEmpty() || s.equals("-") || (hex && s.startsWith("-"))) {
            return null;
        }
        int v;
        try {
            v = Integer.parseInt(s, hex ? 16 : 10);
        } catch (final Throwable t) {
            if (hex) {
                return null;
            } else {
                try {
                    v = Integer.parseInt(s, 16);
                } catch (final Throwable u) {
                    return null;
                }
            }
        }
        return (v < minValue || v > maxValue) ? null : v;
    }

    public static int parseInt(Object value, boolean hex, long maxValue) {
        if (value == null) {
            return ParseErrors.EMPTY;
        }
        String s = value.toString().trim();
        if (s.startsWith("$") || s.startsWith("x")) {
            s = s.substring(1);
            hex = true;
        } else if (s.startsWith("0x") || s.startsWith("&H") || s.startsWith("&h")) {
            s = s.substring(2);
            hex = true;
        }
        if (s.isEmpty() || s.equals("-")) {
            return ParseErrors.EMPTY;
        } else if (s.startsWith("-")) {
            return ParseErrors.OUT_OF_RANGE;
        }
        int v = 0;
        try {
            v = Integer.parseInt(s, hex ? 16 : 10);
        } catch (Throwable t) {
            if (hex) {
                return ParseErrors.NOT_A_NUMBER;
            } else {
                try {
                    v = Integer.parseInt(s, 16);
                } catch (Throwable u) {
                    return ParseErrors.NOT_A_NUMBER;
                }
            }
        }
        return (v < 0 || v > maxValue) ? ParseErrors.OUT_OF_RANGE : v;
    }

    public static String makeEmpty(final String str) {
        return isBlank(str) ? "" : str;
    }

    public static boolean isBlank(final String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String replaceBlank(final String value,
                                      final String replacement) {
        return isBlank(value) ? replacement : value;
    }

    public static String replaceBlank(final String value) {
        return replaceBlank(value, "");
    }

    public static boolean isHexDigit(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    public static String toYesNo(final boolean value) {
        return value ? "yes" : "no";
    }

    public static void appendUnsignedInt(final StringBuilder sb,
                                         final int value) {
        appendUnsignedInt(sb, "%d", value);
    }

    public static void appendUnsignedInt(final StringBuilder sb,
                                         final String format, final int value) {
        append(sb, format, ((long) value) & 0xFFFFFFFFL);
    }

    public static void append(final StringBuilder sb, final String line,
                              final Object... args) {
        sb.append(String.format(line, args));
    }

    public static void appendLine(StringBuilder sb) {
        sb.append(System.lineSeparator());
    }

    public static void appendLine(final StringBuilder sb, final String line,
                                  final Object... args) {
        append(sb, line, args);
        appendLine(sb);
    }

    public static boolean compareStrings(final String str1, final int[] str2) {
        if (str1 == null || str2 == null || str1.length() > str2.length) {
            return false;
        }
        for (int i = str1.length() - 1; i >= 0; i--) {
            if (str1.charAt(i) != str2[i]) {
                return false;
            }
        }
        return true;
    }

    public static String replaceNewlines(String str) {
        StringBuilder sb = new StringBuilder();
        boolean appendNewline = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\n' || ch == '\r') {
                appendNewline = true;
            } else {
                if (appendNewline) {
                    appendNewline = false;
                    sb.append('\n');
                }
                sb.append(ch);
            }
        }
        if (appendNewline) {
            appendNewline = false;
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String removeNewlines(final String str) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            if (!(ch == '\n' || ch == '\r')) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String removeWhitespaces(final String str) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            if (!Character.isWhitespace(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static int parseInt(final String value) {
        return parseInt(value, 0);
    }

    public static int parseInt(final String value, final int defaultValue) {
        try {
            return isBlank(value) ? defaultValue : Integer.parseInt(value);
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static int findMatch(final String[] values, final String target) {
        return findMatch(values, target, 0, values.length);
    }

    public static int findMatch(final String[] values, final String target,
                                final int offset, final int length) {
        for (int i = length - 1; i >= 0; i--) {
            if (target.equalsIgnoreCase(values[offset + i])) {
                return offset + i;
            }
        }
        return -1;
    }

    public static int findContaining(final String[] values,
                                     final String keyword) {
        return findContaining(values, keyword, 0, values.length);
    }

    public static int findContaining(final String[] values, final String keyword,
                                     final int offset, final int length) {
        for (int i = length - 1; i >= 0; i--) {
            if (values[offset + i].toLowerCase().contains(keyword)) {
                return offset + i;
            }
        }
        return -1;
    }

    public static IntPoint getTextDimensions(final String multilineString) {
        int maxLineLength = 0;
        int lineIndex = 0;
        int lines = 0;
        char b = 0;
        final int length = multilineString.length();
        for (int i = 0; i <= length; i++) {
            final char c = i == length ? '\n' : multilineString.charAt(i);
            if (c == '\n' || c == '\r') {
                if ((b == '\r' && c == '\n') || (b == '\n' && c == '\r')) {
                    b = 0;
                } else {
                    maxLineLength = max(i - lineIndex, maxLineLength);
                    lines++;
                    b = c;
                }
                lineIndex = i + 1;
            } else {
                b = c;
            }
        }
        return new IntPoint(maxLineLength, lines);
    }
}
