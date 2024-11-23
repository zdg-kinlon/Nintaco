package nintaco.util;

import java.io.*;
import java.util.*;

public final class CsvUtil {

    private CsvUtil() {
    }

    public static List<List<String>> convert(final String[][] lines) {

        final List<List<String>> result = new ArrayList<>();

        for (final String[] line : lines) {
            final List<String> row = new ArrayList<>();
            result.add(row);
            for (final String str : line) {
                row.add(str);
            }
        }

        return result;
    }

    public static String[][] convert(final List<List<String>> lines) {

        int maxSize = 0;
        for (final List<String> line : lines) {
            maxSize = Math.max(line.size(), maxSize);
        }

        final String[][] result = new String[lines.size()][maxSize];
        for (int i = lines.size() - 1; i >= 0; i--) {
            final List<String> line = lines.get(i);
            for (int j = line.size() - 1; j >= 0; j--) {
                result[i][j] = line.get(j);
            }
        }

        return result;
    }

    public static void write(final PrintStream out,
                             final List<List<String>> lines) throws Throwable {

        for (final List<String> line : lines) {
            final StringBuilder sb = new StringBuilder();
            for (final String str : line) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                append(str, sb);
            }
            out.println(sb);
        }
    }

    private static void append(String str, final StringBuilder sb) {
        if (str == null) {
            return;
        }
        str = str.trim();
        outer:
        {
            for (int i = str.length() - 1; i >= 0; i--) {
                final char c = str.charAt(i);
                if (c == '"' || c == ',' || c == '\n' || c == '\r') {
                    break outer;
                }
            }
            sb.append(str);
            return;
        }
        sb.append('"');
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c == '"') {
                sb.append("\"\"");
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
    }

    public static List<List<String>> read(final BufferedReader br)
            throws Throwable {

        final StringBuilder sb = new StringBuilder();
        String input = null;
        while ((input = br.readLine()) != null) {
            sb.append(input).append('\n');
        }
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (sb.charAt(i) == '\r') {
                if (i > 0 && sb.charAt(i - 1) == '\n') {
                    sb.delete(i, i + 1);
                } else {
                    sb.setCharAt(i, '\n');
                }
            } else if (sb.charAt(i) == '\n') {
                if (i > 0 && sb.charAt(i - 1) == '\r') {
                    sb.delete(i, i + 1);
                    sb.setCharAt(i - 1, '\n');
                }
            }
        }

        boolean insideQuotes = false;
        boolean insideValue = false;
        boolean advance = false;
        boolean rightOfComma = false;
        final StringBuilder s = new StringBuilder();
        final List<List<String>> lines = new ArrayList<>();
        List<String> line = new ArrayList<>();
        for (int i = 0; i < sb.length(); i++) {
            final char c = sb.charAt(i);
            if (advance) {
                if (c == ',') {
                    rightOfComma = true;
                    advance = false;
                } else if (c == '\n') {
                    if (rightOfComma) {
                        line.add(null);
                        rightOfComma = false;
                    }
                    lines.add(line);
                    line = new ArrayList<>();
                    advance = false;
                }
                continue;
            }
            if (!insideValue) {
                if (Character.isWhitespace(c)) {
                    if (c == '\n') {
                        if (rightOfComma) {
                            line.add(null);
                            rightOfComma = false;
                        }
                        lines.add(line);
                        line = new ArrayList<>();
                    }
                    continue;
                } else {
                    insideValue = true;
                    rightOfComma = false;
                    if (c == '"') {
                        insideQuotes = true;
                        continue;
                    }
                }
            }
            if (insideQuotes) {
                if (c == '"') {
                    if (sb.charAt(i + 1) == '"') {
                        s.append('"');
                        i++;
                    } else {
                        insideQuotes = false;
                        insideValue = false;
                        advance = true;
                        line.add(s.toString().trim());
                        s.setLength(0);
                    }
                } else {
                    s.append(c);
                }
            } else if (c == ',' || c == '\n') {
                insideValue = false;
                line.add(s.toString().trim());
                s.setLength(0);
                if (c == '\n') {
                    if (rightOfComma) {
                        line.add(null);
                        rightOfComma = false;
                    }
                    lines.add(line);
                    line = new ArrayList<>();
                } else {
                    rightOfComma = true;
                }
            } else {
                s.append(c);
            }
        }

        return lines;
    }
}
