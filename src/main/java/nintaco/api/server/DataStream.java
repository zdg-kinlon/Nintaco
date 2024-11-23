package nintaco.api.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataStream {

    public static final int ARRAY_LENGTH = 1024;

    private final DataOutputStream out;
    private final DataInputStream in;

    public DataStream(final DataOutputStream out, final DataInputStream in) {
        this.out = out;
        this.in = in;
    }

    public void writeByte(final int value) throws Throwable {
        out.writeByte(value);
    }

    public int readByte() throws Throwable {
        return in.readUnsignedByte();
    }

    public void writeInt(final int value) throws Throwable {
        out.writeInt(value);
    }

    public int readInt() throws Throwable {
        return in.readInt();
    }

    public void writeIntArray(final int[] array) throws Throwable {
        out.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            out.writeInt(array[i]);
        }
    }

    public int readIntArray(final int[] array) throws Throwable {
        final int length = in.readInt();
        if (length < 0 || length > array.length) {
            in.close();
            out.close();
            throw new IOException("Invalid array length: " + length);
        }
        for (int i = 0; i < length; i++) {
            array[i] = in.readInt();
        }
        return length;
    }

    public void writeBoolean(final boolean value) throws Throwable {
        out.writeBoolean(value);
    }

    public boolean readBoolean() throws Throwable {
        return in.readBoolean();
    }

    public void writeChar(final char value) throws Throwable {
        out.writeByte(value);
    }

    public char readChar() throws Throwable {
        return (char) in.readUnsignedByte();
    }

    public void writeCharArray(final char[] array) throws Throwable {
        out.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            out.writeByte(array[i]);
        }
    }

    public int readCharArray(final char[] array) throws Throwable {
        final int length = in.readInt();
        if (length < 0 || length > array.length) {
            in.close();
            out.close();
            throw new IOException("Invalid array length: " + length);
        }
        for (int i = 0; i < length; i++) {
            array[i] = (char) in.readUnsignedByte();
        }
        return length;
    }

    public void writeString(final String value) throws Throwable {
        final int length = value.length();
        out.writeInt(length);
        for (int i = 0; i < length; i++) {
            out.writeByte(value.charAt(i));
        }
    }

    public String readString() throws Throwable {
        final int length = in.readInt();
        if (length < 0 || length > ARRAY_LENGTH) {
            in.close();
            out.close();
            throw new IOException("Invalid array length: " + length);
        }
        final char[] cs = new char[length];
        for (int i = 0; i < length; i++) {
            cs[i] = (char) in.readUnsignedByte();
        }
        return new String(cs);
    }

    public void writeStringArray(final String[] array) throws Throwable {
        out.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            writeString(array[i]);
        }
    }

    public int readStringArray(final String[] array) throws Throwable {
        final int length = in.readInt();
        if (length < 0 || length > array.length) {
            in.close();
            out.close();
            throw new IOException("Invalid array length: " + length);
        }
        for (int i = 0; i < length; i++) {
            array[i] = readString();
        }
        return length;
    }

    public String[] readDynamicStringArray() throws Throwable {
        final int length = in.readInt();
        if (length < 0 || length > ARRAY_LENGTH) {
            in.close();
            out.close();
            throw new IOException("Invalid array length: " + length);
        }
        final String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            array[i] = readString();
        }
        return array;
    }

    public void flush() throws Throwable {
        out.flush();
    }
}