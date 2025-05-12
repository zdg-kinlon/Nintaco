package cn.kinlon.emu.gui.hexeditor;

import java.io.Serializable;
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

    public String getValue() {
        return value;
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
}
