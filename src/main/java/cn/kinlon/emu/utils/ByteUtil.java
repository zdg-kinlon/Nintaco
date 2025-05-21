package cn.kinlon.emu.utils;

public class ByteUtil {

    public final static int U8_MAX_VALUE = 0xff;
    public final static int U16_MAX_VALUE = 0xffff;

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
