package nintaco.files;

import nintaco.MessageException;

import java.io.*;
import java.util.Locale;

import static java.lang.Math.max;
import static nintaco.files.FileType.FDS;
import static nintaco.util.MathUtil.clamp;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.StringUtil.appendLine;
import static nintaco.util.StringUtil.compareStrings;

public class FdsFile implements IFile, Serializable {

    public static final String HEADER_ID = "FDS\u001A";
    public static final String DISK_VERIFICATION = "*NINTENDO-HVC*";
    public static final int SIDE_SIZE = 65500;
    private static final long serialVersionUID = 0;
    private final String fileName;
    private final String description;
    private final String entryFileName;
    private final String archiveFileName;
    private transient int[][] diskData;
    private transient int[] bios;

    private int[] header;
    private final long fileSize;

    public FdsFile(final DataInputStream in, final long fileSize,
                   final String entryFileName, final String archiveFileName,
                   final int[] bios) throws Throwable {

        this.fileSize = fileSize;
        this.bios = bios;
        this.entryFileName = entryFileName;
        this.archiveFileName = archiveFileName;
        this.fileName = FileUtil.getFileName(entryFileName)
                .toLowerCase(Locale.ENGLISH);
        final int totalSides;

        in.mark(256);
        header = new int[16];
        readByteArray(in, header, 0, header.length, true);
        if (compareStrings(HEADER_ID, header)) {
            totalSides = clamp(header[4], 1, 8);
        } else {
            // missing FDS header
            header = new int[0];
            in.reset();
            in.skipBytes(1);
            if (DISK_VERIFICATION.equals(readString(in,
                    DISK_VERIFICATION.length()))) {
                totalSides = clamp((int) (max(SIDE_SIZE, fileSize)
                        / SIDE_SIZE), 1, 8);
            } else {
                throw new MessageException(
                        "Not Family Computer Disk System file format.");
            }
            in.reset();
        }

        diskData = new int[totalSides][0x10000];
        for (int i = 0; i < totalSides; i++) {
            readByteArray(in, diskData[i], 0, SIDE_SIZE, true);
        }

        StringBuilder sb = new StringBuilder();
        if (archiveFileName != null) {
            appendLine(sb, "File name: %s <%s>",
                    FileUtil.getFileName(archiveFileName),
                    FileUtil.getFileName(entryFileName));
            appendLine(sb, "Directory: %s",
                    FileUtil.getDirectoryPath(archiveFileName));
        } else {
            appendLine(sb, "File name: %s", FileUtil.getFileName(entryFileName));
            appendLine(sb, "Directory: %s",
                    FileUtil.getDirectoryPath(entryFileName));
        }
        appendLine(sb, "File format: FDS");
        appendLine(sb, "Disk sides: %d", diskData.length);
        description = sb.toString();
    }

    public String getEntryFileName() {
        return entryFileName;
    }

    public String getArchiveFileName() {
        return archiveFileName;
    }

    public String getFileName() {
        return fileName;
    }

    public int[][] getDiskData() {
        return diskData;
    }

    public int[] getBios() {
        return bios;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int getFileType() {
        return FDS;
    }

    @Override
    public String toString() {
        return description;
    }

    public int[] getFileContents() {
        final int[] data = new int[(int) fileSize];
        System.arraycopy(header, 0, data, 0, header.length);
        for (int side = 0, offset = header.length; side < diskData.length; side++,
                offset += SIDE_SIZE) {
            final int length = Math.min(data.length - offset, SIDE_SIZE);
            if (length > 0) {
                System.arraycopy(diskData[side], 0, data, offset, length);
            } else {
                break;
            }
        }
        return data;
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        bios = readByteArray(in);
        diskData = read2DByteArray(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeByteArray(out, bios);
        write2DByteArray(out, diskData);
    }
}
