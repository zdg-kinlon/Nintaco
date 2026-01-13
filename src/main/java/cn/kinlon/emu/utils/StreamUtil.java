package cn.kinlon.emu.utils;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;
import static java.nio.charset.StandardCharsets.*;

public final class StreamUtil {

    private static interface SparseBlockType {
        int EMPTY = 0;
        int SPARSE = 1;
        int FULL = 2;
    }

    private static final int BYTE_ARRAY_OUTPUT_INITIAL_SIZE = 0x10000;
    private static final int SPARSE_BLOCK_SIZE = 1024;
    private static final int SPARSE_THRESHOLD = SPARSE_BLOCK_SIZE / 3;

    private StreamUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static String readNullTerminatedString(final InputStream in,
                                                  final int blockSize) throws Throwable {
        return readNullTerminatedString(in, blockSize, 0, true);
    }

    public static String readNullTerminatedString(final InputStream in,
                                                  final int blockSize, final int startIndex,
                                                  final boolean readUntilBlockEnd) throws Throwable {
        final StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < blockSize; i++) {
            final int ch = in.read();
            if (ch < 0) {
                throw new EOFException();
            } else if (ch == 0) {
                if (readUntilBlockEnd) {
                    for (i++; i < blockSize && in.read() >= 0; i++) ;
                }
                break;
            } else {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }

    public static String[] readNullTerminatedStrings(final InputStream in,
                                                     final int blockSize) throws Throwable {
        return readNullTerminatedStrings(in, blockSize, Integer.MAX_VALUE);
    }

    public static String[] readNullTerminatedStrings(final InputStream in,
                                                     final int blockSize, final int maxStrings) throws Throwable {
        final List<String> strings = new ArrayList<>();
        for (int i = 0; i < blockSize; ) {
            String str = readNullTerminatedString(in, blockSize, i, false);
            i += str.length() + 1;
            strings.add(str);
            if (strings.size() == maxStrings) {
                for (; i < blockSize && in.read() >= 0; i++) ;
                break;
            }
        }
        String[] result = new String[strings.size()];
        strings.toArray(result);
        return result;
    }

    public static int readInt16LE(final InputStream in) throws Throwable {
        final int b0 = in.read();
        final int b1 = in.read();
        if ((b0 | b1) < 0) {
            throw new EOFException();
        }
        return (b1 << 8) | b0;
    }

    public static int readInt32LE(final InputStream in) throws Throwable {
        final int b0 = in.read();
        final int b1 = in.read();
        final int b2 = in.read();
        final int b3 = in.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException();
        }
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public static void readBytes(InputStream in, int[] bytes) throws Throwable {
        readBytes(in, bytes, 0, bytes.length);
    }

    public static void readBytes(InputStream in, int[] bytes, int offset,
                                 int length) throws Throwable {
        for (int i = 0; i < length; i++) {
            int value = in.read();
            if (value < 0) {
                throw new EOFException();
            }
            bytes[offset + i] = value;
        }
    }

    public static String readString(DataInput in, int length) throws Throwable {
        final byte[] data = new byte[length];
        in.readFully(data);
        return new String(data, ISO_8859_1);
    }

    public static void writeString(DataOutput out, String str) throws Throwable {
        out.write(str.getBytes(ISO_8859_1));
    }

    public static void writeBytes(OutputStream out, int[] bytes)
            throws IOException {
        writeBytes(out, bytes, 0, bytes.length);
    }

    public static void writeBytes(OutputStream out, int[] bytes, int offset,
                                  int length) throws IOException {
        for (int i = 0; i < length; i++) {
            final int index = i + offset;
            out.write(bytes != null && index < bytes.length ? bytes[index] : 0);
        }
    }
    
    public static Object readObject(final byte[] data) throws Throwable {
        if (data == null) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                data))) {
            return ois.readObject();
        }
    }

    public static int[] readSparseByteArray(final DataInput in)
            throws IOException {
        int[] data = null;
        int length = in.readInt();
        if (length >= 0) {
            data = new int[length];
            for (int i = 0; i < data.length; i += SPARSE_BLOCK_SIZE) {
                readSparseBlock(in, data, i);
            }
        }
        return data;
    }

    public static void readSparseBlock(DataInput in, int[] data, int index)
            throws IOException {
        readSparseBlock(in, data, index, 0);
    }

    public static void readSparseBlock(DataInput in, int[] data, int index,
                                       int fill) throws IOException {

        final int BLOCK_SIZE = min(SPARSE_BLOCK_SIZE, data.length - index);

        for (int i = BLOCK_SIZE - 1; i >= 0; i--) {
            data[i + index] = fill;
        }
        switch (in.readUnsignedByte()) {
            case SparseBlockType.EMPTY:
                break;
            case SparseBlockType.SPARSE:
                for (int i = in.readUnsignedShort() - 1; i >= 0; i--) {
                    final int offset = in.readShort();
                    data[offset + index] = in.readUnsignedByte();
                }
                break;
            case SparseBlockType.FULL:
                for (int i = 0; i < BLOCK_SIZE; i++) {
                    data[i + index] = in.readUnsignedByte();
                }
                break;
        }
    }

    public static void writeSparseByteArray(DataOutput out, int[] data)
            throws IOException {
        writeSparseByteArray(out, data, null);
    }

    public static void writeSparseByteArray(DataOutput out, int[] data,
                                            int[] source) throws IOException {
        if (data == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i += SPARSE_BLOCK_SIZE) {
                writeSparseBlock(out, data, source, i);
            }
        }
    }

    public static void writeSparseBlock(DataOutput out, int[] data,
                                        int[] source, int index) throws IOException {

        final int remaining = data.length - index;
        final int BLOCK_SIZE;
        final int THRESHOLD;
        if (remaining < SPARSE_BLOCK_SIZE) {
            BLOCK_SIZE = remaining;
            THRESHOLD = remaining / 3;
        } else {
            BLOCK_SIZE = SPARSE_BLOCK_SIZE;
            THRESHOLD = SPARSE_THRESHOLD;
        }

        int count = 0;
        if (source == null) {
            for (int i = BLOCK_SIZE - 1; i >= 0; --i) {
                if (data[i + index] != 0 && ++count >= THRESHOLD) {
                    break;
                }
            }
        } else {
            for (int i = BLOCK_SIZE - 1; i >= 0; --i) {
                if (data[i + index] != source[i + index] && ++count >= THRESHOLD) {
                    break;
                }
            }
        }
        if (count == 0) {
            out.writeByte(SparseBlockType.EMPTY);
        } else if (count < THRESHOLD) {
            out.writeByte(SparseBlockType.SPARSE);
            out.writeShort(count);
            if (source == null) {
                for (int i = 0; i < BLOCK_SIZE; ++i) {
                    if (data[i + index] != 0) {
                        out.writeShort(i);
                        out.writeByte(data[i + index]);
                    }
                }
            } else {
                for (int i = 0; i < BLOCK_SIZE; ++i) {
                    if (data[i + index] != source[i + index]) {
                        out.writeShort(i);
                        out.writeByte(data[i + index]);
                    }
                }
            }
        } else {
            out.writeByte(SparseBlockType.FULL);
            for (int i = 0; i < BLOCK_SIZE; ++i) {
                out.writeByte(data[i + index]);
            }
        }
    }

    public static void writeByteArray(final DataOutput out, final int[] data)
            throws IOException {
        if (data == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeByte(data[i]);
            }
        }
    }

    public static void write2DByteArray(final DataOutput out, final int[][] data)
            throws IOException {
        if (data == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                writeByteArray(out, data[i]);
            }
        }
    }

    public static int[] readByteArray(final DataInput in) throws IOException {
        int[] data = null;
        final int length = in.readInt();
        if (length >= 0) {
            data = new int[length];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readUnsignedByte();
            }
        }
        return data;
    }

    public static int[][] read2DByteArray(final DataInput in)
            throws IOException {
        int[][] data = null;
        final int length = in.readInt();
        if (length >= 0) {
            data = new int[length][];
            for (int i = 0; i < data.length; i++) {
                data[i] = readByteArray(in);
            }
        }
        return data;
    }

    public static void readByteArray(final InputStream in, final int[] data)
            throws IOException {
        readByteArray(in, data, 0, data.length, false);
    }

    public static void readByteArray(final InputStream in, final int[] data,
                                     final int offset, final int length, final boolean ignoreEOF)
            throws IOException {
        for (int i = 0; i < length; i++) {
            final int value = in.read();
            if (value < 0) {
                if (ignoreEOF) {
                    return;
                } else {
                    throw new EOFException();
                }
            } else {
                data[offset + i] = value;
            }
        }
    }

    // Closes InputStream after copy, but leaves OutputStream open.
    public static void copy(final InputStream in, final OutputStream out,
                            final byte[] buffer) throws Throwable {

        while (true) {
            final int length = in.read(buffer);
            if (length < 0) {
                break;
            }
            out.write(buffer, 0, length);
        }
        in.close();
    }
}
