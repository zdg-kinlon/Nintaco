package nintaco.gui.hexeditor;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class SearchText implements Serializable {

    private static final long serialVersionUID = 0;

    private final String value;
    private final boolean hex;

    public SearchText(final String value, final boolean hex) {
        this.value = value;
        this.hex = hex;
    }

    public static String toString(int[] value, boolean hex, CharTable charTable) {
        final StringBuilder sb = new StringBuilder();
        if (hex) {
            for (int i = 0; i < value.length; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(String.format("%02X", value[i]));
            }
        } else {
            for (int i = 0; i < value.length; i++) {
                sb.append(charTable.getChar(value[i]));
            }
        }
        return sb.toString();
    }

    public static int[] parse(String input, boolean hex, CharTable charTable) {
        int[] result;
        if (input == null) {
            return new int[0];
        } else if (hex) {
            input = input.replaceAll("\\s+", "");
            result = new int[input.length() >> 1];
            for (int i = result.length - 1; i >= 0; i--) {
                int value = 0;
                try {
                    value = Integer.parseInt(input.substring(i << 1, (i << 1) + 2), 16);
                } catch (Throwable t) {
                }
                result[i] = value;
            }
        } else {
            result = new int[input.length()];
            for (int i = input.length() - 1; i >= 0; i--) {
                result[i] = charTable.getValue(input.charAt(i));
                if (result[i] < 0) {
                    result[i] = 0;
                }
            }
        }
        return result;
    }

    public String getValue() {
        return value;
    }

    public boolean isHex() {
        return hex;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.value);
        hash = 59 * hash + (this.hex ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        final SearchText other = (SearchText) obj;
        return value.equals(other.value) && hex == other.hex;
    }

    public String toString(boolean hexString, CharTable charTable) {
        if (hexString || hex) {
            return toString(parse(value, hex, charTable), hexString, charTable);
        } else {
            return value;
        }
    }

    public int[] parseLower(CharTable charTable) {
        return parse(value.toLowerCase(Locale.ENGLISH), hex, charTable);
    }

    public int[] parseUpper(CharTable charTable) {
        return parse(value.toUpperCase(Locale.ENGLISH), hex, charTable);
    }

    public int[] parse(CharTable charTable) {
        return parse(value, hex, charTable);
    }
}
