package cn.kinlon.emu.utils;

public final class StringUtils {
    
    private StringUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static String makeEmpty(final String str) {
        return isBlank(str) ? "" : str;
    }

    public static boolean isBlank(final String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String toYesNo(final boolean value) {
        return value ? "yes" : "no";
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

    public static int findContaining(final String[] values, final String keyword,
                                     final int offset, final int length) {
        for (int i = length - 1; i >= 0; i--) {
            if (values[offset + i].toLowerCase().contains(keyword)) {
                return offset + i;
            }
        }
        return -1;
    }
}
