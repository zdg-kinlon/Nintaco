package nintaco.cheats;

import java.util.Locale;

public final class GameGenie {

    private static final char[] ALPHABET = "APZLGITYEOXUKSVN".toCharArray();
    private static final int[] HEX_VALUES = new int[256];

    static {
        for (int i = HEX_VALUES.length - 1; i >= 0; i--) {
            HEX_VALUES[i] = -1;
        }
        for (int i = ALPHABET.length - 1; i >= 0; i--) {
            HEX_VALUES[ALPHABET[i]] = i;
        }
    }

    private GameGenie() {
    }

    private static int getHexValue(final char c) {
        return HEX_VALUES[c & 0xFF];
    }

    private static int[] getHexValues(String code) {
        code = code.toUpperCase(Locale.ENGLISH);
        final int[] n = new int[code.length()];
        for (int i = 0; i < n.length; i++) {
            n[i] = getHexValue(code.charAt(i));
            if (n[i] < 0) {
                return null;
            }
        }
        return n;
    }

    public static Cheat convert(final String code) {
        if (code == null || (code.length() != 6 && code.length() != 8)) {
            return null;
        }

        final int[] n = getHexValues(code);
        if (n == null) {
            return null;
        }

        final int dataValue;
        final int compareValue;
        final int address = 0x8000
                | ((n[1] & 8) << 4)
                | ((n[2] & 7) << 4)
                | (n[3] & 8) | ((n[3] & 7) << 12)
                | ((n[4] & 8) << 8) | (n[4] & 7)
                | ((n[5] & 7) << 8);

        final int n0 = ((n[0] & 8) << 4) | (n[0] & 7);
        final int n1 = ((n[1] & 7) << 4);
        if (code.length() == 8) {
            dataValue = n0 | n1
                    | (n[7] & 8);
            compareValue = (n[5] & 8)
                    | ((n[6] & 8) << 4) | (n[6] & 7)
                    | ((n[7] & 7) << 4);
        } else {
            dataValue = n0 | n1
                    | (n[5] & 8);
            compareValue = -1;
        }

        return new Cheat(address, dataValue, compareValue);
    }

    public static String convert(final Cheat cheat) {

        if (cheat == null) {
            return null;
        }

        return convert(cheat.getAddress(), cheat.getDataValue(),
                cheat.getCompareValue(), cheat.hasCompareValue());
    }

    public static String convert(final int address, final int dataValue,
                                 final int compareValue, final boolean hasCompareValue) {

        if (address < 0x8000) {
            return null;
        }

        final int[] n = new int[hasCompareValue ? 8 : 6];

        n[0] = ((dataValue >> 4) & 8) | (dataValue & 7);
        n[1] = ((address >> 4) & 8) | ((dataValue >> 4) & 7);
        n[2] = ((address >> 4) & 7);
        n[3] = (address & 8) | ((address >> 12) & 7);
        n[4] = ((address >> 8) & 8) | (address & 7);
        n[5] = (address >> 8) & 7;

        if (hasCompareValue) {
            n[2] |= 8;
            n[5] |= compareValue & 8;
            n[6] = ((compareValue >> 4) & 8) | (compareValue & 7);
            n[7] = (dataValue & 8) | ((compareValue >> 4) & 7);
        } else {
            n[5] |= dataValue & 8;
        }

        final StringBuilder sb = new StringBuilder();
        for (int i : n) {
            if (i < 0 || i > 15) {
                return null;
            }
            sb.append(ALPHABET[i]);
        }
        return sb.toString();
    }
}
