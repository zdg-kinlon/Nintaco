package cn.kinlon.emu.utils;

public class ByteUtil {

    public static int toU8(int i8) {
        return i8 & 0xff;
    }

    public static byte toi8(int u8) {
        return (byte) u8;
    }

    public static int toU16(int i16) {
        return i16 & 0xffff;
    }
}
