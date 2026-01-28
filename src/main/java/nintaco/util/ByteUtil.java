package nintaco.util;

public class ByteUtil {

    public static int toU8(int i8) {
        return i8 & 0xff;
    }

    public static byte toI8(int u8) {
        return (byte) u8;
    }

    public static int toU16(int i16) {
        return i16 & 0xffff;
    }

    private ByteUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
