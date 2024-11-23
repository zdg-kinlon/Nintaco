package nintaco.files;

import nintaco.MessageException;
import nintaco.preferences.AppPrefs;
import nintaco.util.InvocationContainer;

import java.io.*;
import java.util.*;

import static java.lang.Math.max;
import static nintaco.files.FileUtil.InputStreamListener;
import static nintaco.files.FileUtil.getInputStream;
import static nintaco.util.StreamUtil.readBytes;
import static nintaco.util.StringUtil.compareStrings;
import static nintaco.util.StringUtil.isBlank;

public final class IpsUtil {

    private static final int EOF = 0x454F46;

    private IpsUtil() {
    }

    private static IpsFile loadIpsFile(final FilePath filePath)
            throws Throwable {
        final InvocationContainer<IpsFile> container = new InvocationContainer<>();
        try {
            getInputStream(filePath, (in, fileSize) -> {
                container.setObject(loadIpsFile(in, fileSize));
            });
        } catch (final FileNotFoundException e) {
            throw new MessageException("The patch file does not exist.");
        }
        return container.getObject();
    }

    private static IpsFile loadIpsFile(final String fileName) throws Throwable {
        return loadIpsFile(new File(fileName));
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
                                       final FilePath filePath) throws Throwable {
        return loadFileData(fileType, filePath, -1);
    }

    private static byte[] loadFileData(final String fileType,
                                       final FilePath filePath, final int minFileSize) throws Throwable {
        final InvocationContainer<byte[]> container = new InvocationContainer<>();
        try {
            getInputStream(filePath, (in, fileSize) -> {
                container.setObject(loadFileData(fileType, in, fileSize, minFileSize));
            });
        } catch (final FileNotFoundException e) {
            throw new MessageException("The %s file does not exist.", fileType);
        }
        return container.getObject();
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

    public static void getArchiveInputStream(final String archiveFileName,
                                             final String entryFileName, final InputStreamListener listener)
            throws Throwable {
        FileUtil.getArchiveInputStream(archiveFileName, entryFileName,
                (stream, size) -> {
                    final OpenFileHandle handle = getOpenFileHandle(archiveFileName, stream,
                            size);
                    listener.handleInputStream(handle.getInputStream(),
                            handle.getFileSize());
                });
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

    public static void applyIPS(final String original, final String patch,
                                final String modified) throws Throwable {

        final IpsFile ipsFile = loadIpsFile(FilePath.fromLongString(patch));
        final byte[] data = loadFileData("original",
                FilePath.fromLongString(original), getMinFileSize(ipsFile));
        applyIpsFile(ipsFile, data);

        try (final DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(modified)))) {
            if (ipsFile.truncatedLength >= 0) {
                out.write(data, 0, ipsFile.truncatedLength);
            } else {
                out.write(data);
            }
        } catch (final Throwable t) {
            throw new MessageException("Failed to create the modified file.");
        }
    }

    public static void createIPS(final String original, final String modified,
                                 final String patch) throws Throwable {

        final byte[] originalBytes = loadFileData("original",
                FilePath.fromLongString(original));
        final byte[] modifiedBytes = loadFileData("modified",
                FilePath.fromLongString(modified));

        // Find modified regions.
        final List<IpsRecord> records = new ArrayList<>();
        {
            IpsRecord record = null;
            for (int i = 0; i < modifiedBytes.length; i++) {
                if (i >= originalBytes.length || originalBytes[i] != modifiedBytes[i]) {
                    if (record == null) {
                        if (i == EOF) {
                            record = new IpsRecord(EOF - 1);
                            record.length = 2;
                        } else {
                            record = new IpsRecord(i);
                        }
                        records.add(record);
                    } else {
                        record.length++;
                    }
                } else {
                    record = null;
                }
            }
        }

        // Combine regions that are close together.
        {
            int i = 0;
            while (i + 1 < records.size()) {
                final IpsRecord r1 = records.get(i);
                final IpsRecord r2 = records.get(i + 1);
                if (r2.offset - r1.offset - r1.length < 15) {
                    r1.length = r2.offset + r2.length - r1.offset;
                    records.remove(i + 1);
                } else {
                    i++;
                }
            }
        }

        // RLE encode regions.
        final List<IpsRecord> rles = new ArrayList<>();
        {
            final BitSet regions = new BitSet(modifiedBytes.length);
            for (final IpsRecord record : records) {
                int i = 0;
                while (i < record.length) {
                    final int startIndex = record.offset + i;
                    if (startIndex == EOF) {
                        i++;
                        continue;
                    }
                    final byte startByte = modifiedBytes[startIndex];
                    for (int j = i + 1; j <= record.length; j++) {
                        if (j == record.length
                                || startByte != modifiedBytes[record.offset + j]) {
                            final int spanLength = j - i;
                            if (spanLength > 15) {
                                final IpsRecord r = new IpsRecord(startIndex);
                                r.length = spanLength;
                                r.rle = startByte & 0xFF;
                                rles.add(r);
                                for (int k = 0; k < r.length; k++) {
                                    regions.set(r.offset + k);
                                }
                            }
                            i = j;
                            break;
                        }
                    }
                }
            }
            for (final IpsRecord record : records) {
                IpsRecord r = null;
                for (int i = 0; i < record.length; i++) {
                    final int index = record.offset + i;
                    if (!regions.get(index)) {
                        if (r == null) {
                            if (index == EOF) {
                                r = new IpsRecord(EOF - 1);
                                r.length = 2;
                            } else {
                                r = new IpsRecord(index);
                            }
                            rles.add(r);
                        } else {
                            r.length++;
                        }
                    } else {
                        r = null;
                    }
                }
            }

            // Split long regions.
            {
                for (int i = 0; i < rles.size(); i++) {
                    final IpsRecord record = rles.get(i);
                    if (record.length > 0xFFFF) {
                        final int index = record.offset + 0xFFFF;
                        final IpsRecord r;
                        if (index == EOF) {
                            r = new IpsRecord(EOF - 1);
                            r.length = record.length - 0xFFFE;
                            record.length = 0xFFFE;
                        } else {
                            r = new IpsRecord(index);
                            r.length = record.length - 0xFFFF;
                            record.length = 0xFFFF;
                        }
                        r.rle = record.rle;
                        rles.add(r);
                    }
                }
            }

            // Remove unnecessary regions.
            {
                outer:
                for (final Iterator<IpsRecord> i = rles.iterator();
                     i.hasNext(); ) {
                    final IpsRecord r = i.next();
                    for (int j = r.length - 1; j >= 0; j--) {
                        final int index = r.offset + j;
                        if (index >= originalBytes.length
                                || originalBytes[index] != modifiedBytes[index]) {
                            continue outer;
                        }
                    }
                    i.remove();
                }
            }

            // Left trim regions.
            {
                for (final IpsRecord r : rles) {
                    int firstDifferenceIndex = 0;
                    for (int i = 0; i < r.length; i++) {
                        final int index = r.offset + i;
                        if (index >= originalBytes.length
                                || originalBytes[index] != modifiedBytes[index]) {
                            firstDifferenceIndex = i;
                            break;
                        }
                    }
                    r.offset += firstDifferenceIndex;
                    r.length -= firstDifferenceIndex;
                    if (r.offset == EOF) {
                        r.offset--;
                        r.length++;
                    }
                }
            }

            // Right trim regions.
            {
                for (final IpsRecord r : rles) {
                    int firstDifferenceIndex = r.length - 1;
                    for (int i = r.length - 1; i >= 0; i--) {
                        final int index = r.offset + i;
                        if (index >= originalBytes.length
                                || originalBytes[index] != modifiedBytes[index]) {
                            firstDifferenceIndex = i;
                            break;
                        }
                    }
                    r.length = firstDifferenceIndex + 1;
                }
            }

            // Sort the regions by offset.
            Collections.sort(rles);

            try (final DataOutputStream out = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(patch)))) {
                out.writeBytes("PATCH");
                for (final IpsRecord record : rles) {
                    out.write(record.offset >> 16);
                    out.write(record.offset >> 8);
                    out.write(record.offset);
                    if (record.rle < 0) {
                        out.write(record.length >> 8);
                        out.write(record.length);
                        out.write(modifiedBytes, record.offset, record.length);
                    } else {
                        out.write(0);
                        out.write(0);
                        out.write(record.length >> 8);
                        out.write(record.length);
                        out.write(record.rle);
                    }
                }
                out.writeBytes("EOF");
                if (modifiedBytes.length < originalBytes.length) {
                    out.write(modifiedBytes.length >> 16);
                    out.write(modifiedBytes.length >> 8);
                    out.write(modifiedBytes.length);
                }
            } catch (final Throwable t) {
                throw new MessageException("Failed to create the patch file.");
            }
        }
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

        public IpsRecord() {
        }

        public IpsRecord(final int offset) {
            this.offset = offset;
        }

        @Override
        public int compareTo(final IpsRecord o) {
            return Integer.compare(offset, o.offset);
        }
    }

    private static class IpsFile {
        public List<IpsRecord> records = new ArrayList<>();
        public int truncatedLength = -1;
    }
}
