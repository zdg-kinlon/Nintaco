package nintaco.util;

import java.util.*;

public final class BitUtil {

    private static final Map<Integer, Integer> LOG2S = new HashMap<>();
    private static final int[] BASE2S = new int[32];
    private static final int[] BIT_REVERSED = new int[256];

    static {
        for (int i = 0; i < 32; i++) {
            BASE2S[i] = 1 << i;
            LOG2S.put(BASE2S[i], i);
        }
        for (int i = 0; i < 256; i++) {
            int value = i;
            for (int j = 0; j < 8; j++) {
                BIT_REVERSED[i] = (BIT_REVERSED[i] << 1) | (value & 1);
                value >>= 1;
            }
        }
    }

    public static final int moveBit(final int value, final int sourceBit,
                                    final int destinationBit) {
        return moveBit(value, sourceBit, destinationBit, false);
    }

    public static final int moveBit(final int value, final int sourceBit,
                                    final int destinationBit, final boolean invert) {

        if (invert) {
            return (((value >> sourceBit) & 1) ^ 1) << destinationBit;
        } else {
            return ((value >> sourceBit) & 1) << destinationBit;
        }
    }

    public static final int reverseBits(final int value) {
        return BIT_REVERSED[value];
    }

    public static final int reverseBits16(final int value) {
        return (BIT_REVERSED[value & 0xFF] << 8) | BIT_REVERSED[value >> 8];
    }

    public static final boolean isTrue(final int value) {
        return value != 0;
    }

    public static final boolean isFalse(final int value) {
        return value == 0;
    }

    public static final int toBit(final boolean value) {
        return value ? 1 : 0;
    }

    public static final int toBit(final boolean value, final int bit) {
        return (value ? 1 : 0) << bit;
    }

    public static boolean toBitBool(final int value) {
        return value != 0;
    }

    public static final boolean toBitBool(final int value, final int bit) {
        return ((value >> bit) & 1) != 0;
    }

    public static final boolean getBitBool(final int x, final int bit) {
        return getBit(x, bit) == 1;
    }

    public static final int getBit(final int x, final int bit) {
        return (x >> bit) & 1;
    }

    public static final int setBit(final int x, final int bit) {
        return x | (1 << bit);
    }

    public static final int resetBit(final int x, final int bit) {
        return x & ~(1 << bit);
    }

    public static final int setBit(final int x, final int bit,
                                   final boolean value) {
        if (value) {
            return x | (1 << bit);
        } else {
            return x & ~(1 << bit);
        }
    }

    public static final int setBit(final int x, final int bit, final int value) {
        if (value != 0) {
            return x | (1 << bit);
        } else {
            return x & ~(1 << bit);
        }
    }

    public static final int toggleBit(final int x, final int bit) {
        return x ^ (1 << bit);
    }

    public static final boolean isBase2(final int value) {
        for (int i = BASE2S.length - 1; i >= 0; i--) {
            if (value == BASE2S[i]) {
                return true;
            }
        }
        return false;
    }

    public static final int ceilBase2(final int value) {
        for (int i = 0; i < BASE2S.length; i++) {
            if (BASE2S[i] >= value) {
                return BASE2S[i];
            }
        }
        return value;
    }

    public static final int log2(final int value) {
        return LOG2S.get(value);
    }

    private BitUtil() {
    }
}
