package cn.kinlon.emu.utils;

public class ByteUtil {

    public final static int U8_MAX_VALUE = 0xff;
    public final static int U16_MAX_VALUE = 0xffff;

    public final static int U16_HIGH_MASK = 0xff00;
    public final static int U16_LOW_MASK = U8_MAX_VALUE;

    public static int toU8(int i8) {
        return i8 & U8_MAX_VALUE;
    }

    public static byte toi8(int u8) {
        return (byte) u8;
    }

    public static int toU16(int i16) {
        return i16 & U16_MAX_VALUE;
    }
}
