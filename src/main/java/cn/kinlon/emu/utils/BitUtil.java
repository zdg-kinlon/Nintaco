package cn.kinlon.emu.utils;

public final class BitUtil {

    private static final int[] BIT_REVERSED = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int value = i;
            for (int j = 0; j < 8; j++) {
                BIT_REVERSED[i] = (BIT_REVERSED[i] << 1) | (value & 1);
                value >>= 1;
            }
        }
    }

    public static int moveBit(final int value, final int sourceBit, final int destinationBit) {
        return moveBit(value, sourceBit, destinationBit, false);
    }

    public static int moveBit(final int value, final int sourceBit, final int destinationBit, final boolean invert) {
        if (invert) {
            return (((value >> sourceBit) & 1) ^ 1) << destinationBit;
        } else {
            return ((value >> sourceBit) & 1) << destinationBit;
        }
    }

    public static int reverseBits(int value) {
        value = (value & 0b0101_0101) << 1 | (value >>> 1) & 0b0101_0101;
        value = (value & 0b0011_0011) << 2 | (value >>> 2) & 0b0011_0011;
        value = (value & 0b0000_1111) << 4 | (value >>> 4) & 0b0000_1111;
        return value;
    }

    public static int toBit(final boolean value) {
        return value ? 1 : 0;
    }

    public static boolean toBitBool(final int value) {
        return (value & 1) != 0;
    }

    public static boolean getBitBool(final int x, final int bit) {
        return getBit(x, bit) != 0;
    }

    public static int getBit(final int x, final int bit) {
        return (x >> bit) & 1;
    }

    public static int setBit(final int x, final int bit) {
        return x | (1 << bit);
    }

    public static int setBit(final int x, final int bit, final boolean value) {
        if (value) {
            return x | (1 << bit);
        } else {
            return x & ~(1 << bit);
        }
    }

    public static int toggleBit(final int x, final int bit) {
        return x ^ (1 << bit);
    }

    public static boolean isBase2(final int value) {
        return (value > 0) && ((value & (value - 1)) == 0);
    }

    public static int ceilBase2(int value) {
        if (value <= 0) {
            return 1;
        }
        value--;
        value |= value >> 0b0_0001;
        value |= value >> 0b0_0010;
        value |= value >> 0b0_0100;
        value |= value >> 0b0_1000;
        value |= value >> 0b1_0000;
        return value + 1;
    }

    public static int log2(final int value) {
        return (int) (Math.log(value) / Math.log(2.0));
    }

    private BitUtil() {
    }
}
