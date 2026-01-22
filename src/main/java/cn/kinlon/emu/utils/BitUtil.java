package cn.kinlon.emu.utils;

public final class BitUtil {

    public static int moveBit(final int value, final int from, final int to) {
        return moveBit(value, from, to, false);
    }

    public static int moveBit(final int value, final int from, final int to, final boolean invert) {
        return (((value >> from) & 1) ^ (invert ? 1 : 0)) << to;
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
        return value != 0;
    }

    public static boolean getBitBool(final int x, final int bit) {
        return toBitBool(getBit(x, bit));
    }

    public static int getBit(final int x, final int bit) {
        return (x >>> bit) & 1;
    }

    public static int setBit(final int x, final int bit) {
        return x | (1 << bit);
    }

    public static int cleanBit(final int x, final int bit) {
        return x & ~(1 << bit);
    }

    public static int setBit(final int x, final int bit, final boolean bool) {
        return bool ? setBit(x, bit) : cleanBit(x, bit);
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
        return Integer.SIZE - Integer.numberOfLeadingZeros(value) - 1;
    }

    private BitUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
