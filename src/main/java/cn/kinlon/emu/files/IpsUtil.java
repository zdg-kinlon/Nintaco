package cn.kinlon.emu.files;

import cn.kinlon.emu.MessageException;
import cn.kinlon.emu.preferences.AppPrefs;

import java.io.*;
import java.util.*;

import static java.lang.Math.max;
import static cn.kinlon.emu.utils.StreamUtil.readBytes;
import static cn.kinlon.emu.utils.StringUtils.compareStrings;
import static cn.kinlon.emu.utils.StringUtils.isBlank;

public final class IpsUtil {

    private static final int EOF = 0x454F46;

    private IpsUtil() {
    }

    private static IpsFile loadIpsFile(final File file) throws Throwable {
        return loadIpsFile(new FileInputStream(file), file.length());
    }

    private static IpsFile loadIpsFile(final InputStream inputStream,
                                       final long fileSize) throws Throwable {
        final IpsFile ipsFile = new IpsFile();
        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(
                inputStream))) {
            final int[] header = new int[5];
            readBytes(in, header);
            if (!compareStrings("PATCH", header)) {
                throw new MessageException("Invalid IPS file: Bad header.");
            }
            int index = 5;
            while (true) {
                final int offset = ((in.readByte() & 0xFF) << 16)
                        | ((in.readByte() & 0xFF) << 8) | (in.readByte() & 0xFF);
                if (offset == EOF) {
                    if (index == fileSize - 6) {
                        ipsFile.truncatedLength = ((in.readByte() & 0xFF) << 16)
                                | ((in.readByte() & 0xFF) << 8) | (in.readByte() & 0xFF);
                        break;
                    } else if (index == fileSize - 3) {
                        break;
                    }
                }
                final IpsRecord record = new IpsRecord(offset);
                record.length = ((in.readByte() & 0xFF) << 8) | (in.readByte() & 0xFF);
                index += 5;
                if (record.length == 0) {
                    record.length = ((in.readByte() & 0xFF) << 8)
                            | (in.readByte() & 0xFF);
                    record.rle = in.readByte() & 0xFF;
                    index += 3;
                } else {
                    record.data = new byte[record.length];
                    in.readFully(record.data);
                    index += record.length;
                }
                ipsFile.records.add(record);
            }
        } catch (final EOFException e) {
            throw new MessageException("Invalid IPS file: Missing or misplaced EOF "
                    + "marker.");
        } catch (final MessageException m) {
            throw m;
        } catch (final Throwable t) {
            //t.printStackTrace();
            throw new MessageException("Failed to read patch file.");
        }

        for (final Iterator<IpsRecord> i = ipsFile.records.iterator();
             i.hasNext(); ) {
            final IpsRecord record = i.next();
            if (record.length == 0) {
                i.remove();
            }
        }
        Collections.sort(ipsFile.records);

        return ipsFile;
    }

    private static byte[] loadFileData(final String fileType,
                                       final InputStream in, final long fileSize, final int minFileSize)
            throws Throwable {
        if (fileSize > 0xFFFFFF) {
            throw new MessageException(
                    "An IPS patch cannot be applied to a file larger than 16 MB.");
        }
        final byte[] data = new byte[max(minFileSize, (int) fileSize)];
        try (final DataInputStream dis = new DataInputStream(
                new BufferedInputStream(in))) {
            dis.readFully(data, 0, (int) fileSize);
        } catch (final Throwable t) {
            throw new MessageException("Failed to read the %s file.", fileType);
        }
        return data;
    }

    private static int getMinFileSize(final IpsFile ipsFile) {
        int minFileSize = ipsFile.truncatedLength;
        for (final IpsRecord record : ipsFile.records) {
            minFileSize = max(minFileSize, record.offset + record.length);
        }
        return minFileSize;
    }

    private static void applyIpsFile(final IpsFile ipsFile, final byte[] data) {
        for (final IpsRecord record : ipsFile.records) {
            if (record.rle >= 0) {
                for (int i = record.length - 1; i >= 0; i--) {
                    data[record.offset + i] = (byte) record.rle;
                }
            } else {
                System.arraycopy(record.data, 0, data, record.offset, record.length);
            }
        }
    }

    public static OpenFileHandle getOpenFileHandle(final String fileName)
            throws Throwable {
        return getOpenFileHandle(fileName, new FileInputStream(fileName),
                new File(fileName).length());
    }

    public static OpenFileHandle getOpenFileHandle(final String sourceFile,
                                                   final InputStream in, final long fileSize) throws Throwable {

        if (isBlank(sourceFile) || fileSize <= 0) {
            return new OpenFileHandle(in, fileSize);
        }

        final File ips = new File(sourceFile + ".ips");
        if (!(ips.exists() && AppPrefs.getInstance().getUserInterfacePrefs()
                .isApplyIpsPatches())) {
            return new OpenFileHandle(in, fileSize);
        }

        final IpsFile ipsFile = loadIpsFile(ips);
        final byte[] data = loadFileData("specified", in, fileSize,
                getMinFileSize(ipsFile));
        applyIpsFile(ipsFile, data);
        final int length = ipsFile.truncatedLength >= 0 ? ipsFile.truncatedLength
                : data.length;
        return new OpenFileHandle(new ByteArrayInputStream(data, 0, length),
                length);
    }

    public static class OpenFileHandle {

        private final InputStream inputStream;
        private final long fileSize;

        public OpenFileHandle(final InputStream inputStream, final long fileSize) {
            this.inputStream = inputStream;
            this.fileSize = fileSize;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public long getFileSize() {
            return fileSize;
        }
    }

    private static class IpsRecord implements Comparable<IpsRecord> {
        public int offset;
        public int length = 1;
        public int rle = -1;
        public byte[] data;

        public IpsRecord(final int offset) {
            this.offset = offset;
        }

        @Override
        public int compareTo(final IpsRecord o) {
            return Integer.compare(offset, o.offset);
        }
    }

    private static class IpsFile {
        public final List<IpsRecord> records = new ArrayList<>();
        public int truncatedLength = -1;
    }
}
