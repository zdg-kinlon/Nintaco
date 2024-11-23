package nintaco.files;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import nintaco.App;
import nintaco.gui.archive.EntryRegion;
import nintaco.preferences.AppPrefs;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.Math.min;
import static nintaco.files.FileType.*;
import static nintaco.util.StreamUtil.readByteArray;
import static nintaco.util.StreamUtil.readString;
import static nintaco.util.StringUtil.compareStrings;
import static nintaco.util.StringUtil.isBlank;

public final class FileUtil {

    private static final int MAX_ARCHIVE_ENTRIES = 10000;
    private static final int MAX_FILE_NUMBER = 10000;

    private static final String FILE_EXTENSION_ZIP = "zip";
    private static final String FILE_EXTENSION_7Z = "7z";
    private static final String FILE_EXTENSION_RAR = "rar";
    private static final String FILE_EXTENSION_TAR = "tar";
    private static final String FILE_EXTENSION_TAR_BZ2 = "tar.bz2";
    private static final String FILE_EXTENSION_TAR_GZ = "tar.gz";
    private static final String FILE_EXTENSION_TAR_LZMA = "tar.lzma";
    private static final String FILE_EXTENSION_TAR_XZ = "tar.xz";
    private static final String FILE_EXTENSION_TAR_Z = "tar.Z";

    private static final String FILE_EXTENSION_TB2 = "tb2";
    private static final String FILE_EXTENSION_TBZ = "tbz";
    private static final String FILE_EXTENSION_TBZ2 = "tbz2";
    private static final String FILE_EXTENSION_TGZ = "tgz";
    private static final String FILE_EXTENSION_TLZ = "tlz";
    private static final String FILE_EXTENSION_TXZ = "txz";
    private static final String FILE_EXTENSION_TZ = "tZ";

    private static final Pattern REVISION_PATTERN
            = Pattern.compile("\\((prg|v|rev)(\\d+)\\)");

    private static final Set<String> ALL_FILE_EXTENSIONS;
    private static final Set<String> WITHIN_ARCHIVE_EXTENSIONS;
    private static final Set<String> ARCHIVE_EXTENSIONS;
    private static final String[] COMPRESSED_TAR_EXTENSIONS = {
            FILE_EXTENSION_TAR_BZ2,
            FILE_EXTENSION_TAR_GZ,
            FILE_EXTENSION_TAR_LZMA,
            FILE_EXTENSION_TAR_XZ,
            FILE_EXTENSION_TAR_Z,
    };
    private static final String[] HEADERS = {
            NesFile.HEADER_ID,
            FdsFile.HEADER_ID,
            UnifFile.HEADER_ID,
            NsfFile.NSF_HEADER_ID,
            NsfFile.NSFE_HEADER_ID,
    };
    private static final int[] FILE_TYPES = {
            NES,
            FDS,
            UNIF,
            NSF,
            NSF,
    };
    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final Pattern GoodDumpPattern = Pattern
            .compile("\\[[^\\]]*\\![^\\]]*\\]");
    private static String workingDir;

    static {
        ARCHIVE_EXTENSIONS = new HashSet<>();
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_ZIP);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_7Z);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_RAR);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TAR);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TAR_BZ2);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TAR_GZ);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TAR_LZMA);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TAR_XZ);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TAR_Z);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TB2);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TBZ);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TBZ2);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TGZ);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TLZ);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TXZ);
        ARCHIVE_EXTENSIONS.add(FILE_EXTENSION_TZ);
        WITHIN_ARCHIVE_EXTENSIONS = new HashSet<>(Arrays.asList("nes", "unf", "unif", "fds", "nsf", "nez"));
        ALL_FILE_EXTENSIONS = new HashSet<>(WITHIN_ARCHIVE_EXTENSIONS);
        ALL_FILE_EXTENSIONS.addAll(ARCHIVE_EXTENSIONS);
    }

    private FileUtil() {
    }

    public static void getInputStream(final FilePath filePath,
                                      final InputStreamListener listener) throws Throwable {
        if (filePath.isArchivedEntry()) {
            getArchiveInputStream(filePath, listener);
        } else {
            final File file = new File(filePath.getEntryPath());
            listener.handleInputStream(new FileInputStream(file), file.length());
        }
    }

    public static void getArchiveInputStream(final FilePath filePath,
                                             final InputStreamListener listener) throws Throwable {
        getArchiveInputStream(filePath.getArchivePath(), filePath.getEntryPath(),
                listener);
    }

    public static void getArchiveInputStream(final FilePath filePath,
                                             final InputStreamListener listener, final Set<String> validExtensions)
            throws Throwable {
        getArchiveInputStream(filePath.getArchivePath(), filePath.getEntryPath(),
                listener, validExtensions);
    }

    public static void getArchiveInputStream(final String archiveFileName,
                                             final String entryFileName, final InputStreamListener listener)
            throws Throwable {
        getArchiveInputStream(archiveFileName, entryFileName, listener,
                WITHIN_ARCHIVE_EXTENSIONS);
    }

    public static void getArchiveInputStream(final String archiveFileName,
                                             final String entryFileName, final InputStreamListener listener,
                                             final Set<String> validExtensions) throws Throwable {
        final String extension = getFileExtension(archiveFileName);
        if (extension == null) {
            throw new IOException("Unknown archive file type.");
        }
        final File archiveFile = new File(archiveFileName);
        if (!archiveFile.exists()) {
            throwFileNotFound(archiveFile, entryFileName);
        }
        switch (extension) {
            case FILE_EXTENSION_ZIP:
                getZipInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_7Z:
                get7ZipInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_RAR:
                getRarInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_TAR:
                getTarInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_TAR_BZ2:
            case FILE_EXTENSION_TB2:
            case FILE_EXTENSION_TBZ:
            case FILE_EXTENSION_TBZ2:
                getTarBz2InputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_TAR_GZ:
            case FILE_EXTENSION_TGZ:
                getTarGzInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_TAR_LZMA:
            case FILE_EXTENSION_TLZ:
                getTarLzmaInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_TAR_XZ:
            case FILE_EXTENSION_TXZ:
                getTarXzInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            case FILE_EXTENSION_TAR_Z:
            case FILE_EXTENSION_TZ:
                getTarZInputStream(archiveFile, entryFileName, listener,
                        validExtensions);
                break;
            default:
                throw new IOException("Unknown archive file type: " + extension);
        }
    }

    private static void getTarInputStream(
            final InputStream in,
            final File archiveFile,
            final String entryFileName,
            final InputStreamListener listener,
            final Set<String> validExtensions)
            throws Throwable {
        try (final TarArchiveInputStream tarInput = new TarArchiveInputStream(in)) {
            TarArchiveEntry entry;
            while ((entry = tarInput.getNextEntry()) != null) {
                if (!entry.isDirectory() && (validExtensions == null
                        || validExtensions.contains(getFileExtension(entry.getName())))
                        && entry.getSize() > 0
                        && entryFileName.equals(entry.getName())) {
                    listener.handleInputStream(tarInput, entry.getSize());
                    return;
                }
            }
        }
        throwFileNotFound(archiveFile, entryFileName);
    }

    private static void getTarInputStream(final File archiveFile,
                                          final String entryFileName, final InputStreamListener listener,
                                          final Set<String> validExtensions) throws Throwable {
        getTarInputStream(new FileInputStream(archiveFile), archiveFile,
                entryFileName, listener, validExtensions);
    }

    private static void getTarBz2InputStream(final File archiveFile,
                                             final String entryFileName, final InputStreamListener listener,
                                             final Set<String> validExtensions) throws Throwable {
        getTarInputStream(new BZip2CompressorInputStream(
                        new FileInputStream(archiveFile)), archiveFile, entryFileName, listener,
                validExtensions);
    }

    private static void getTarGzInputStream(final File archiveFile,
                                            final String entryFileName, final InputStreamListener listener,
                                            final Set<String> validExtensions) throws Throwable {
        getTarInputStream(new GzipCompressorInputStream(
                        new FileInputStream(archiveFile)), archiveFile, entryFileName, listener,
                validExtensions);
    }

    private static void getTarLzmaInputStream(final File archiveFile,
                                              final String entryFileName, final InputStreamListener listener,
                                              final Set<String> validExtensions) throws Throwable {
        getTarInputStream(new LZMACompressorInputStream(
                        new FileInputStream(archiveFile)), archiveFile, entryFileName, listener,
                validExtensions);
    }

    private static void getTarXzInputStream(final File archiveFile,
                                            final String entryFileName, final InputStreamListener listener,
                                            final Set<String> validExtensions) throws Throwable {
        getTarInputStream(new XZCompressorInputStream(
                        new FileInputStream(archiveFile)), archiveFile, entryFileName, listener,
                validExtensions);
    }

    private static void getTarZInputStream(final File archiveFile,
                                           final String entryFileName, final InputStreamListener listener,
                                           final Set<String> validExtensions) throws Throwable {
        getTarInputStream(new ZCompressorInputStream(
                        new FileInputStream(archiveFile)), archiveFile, entryFileName, listener,
                validExtensions);
    }

    private static void getZipInputStream(final File archiveFile,
                                          final String entryFileName, final InputStreamListener listener,
                                          final Set<String> validExtensions) throws Throwable {
        try (final ZipFile zipFile = new ZipFile(archiveFile)) {
            final ZipEntry entry = zipFile.getEntry(entryFileName);
            if (entry == null || entry.isDirectory() || (validExtensions != null
                    && !validExtensions.contains(getFileExtension(entry.getName())))
                    || entry.getSize() == 0) {
                throwFileNotFound(archiveFile, entryFileName);
            }
            listener.handleInputStream(zipFile.getInputStream(entry),
                    entry.getSize());
        }
    }

    private static void get7ZipInputStream(final File archiveFile,
                                           final String entryFileName, final InputStreamListener listener,
                                           final Set<String> validExtensions) throws Throwable {
        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            while (true) {
                final SevenZArchiveEntry entry = sevenZFile.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (!entry.isDirectory() && (validExtensions == null
                        || validExtensions.contains(getFileExtension(entry.getName())))
                        && entry.getSize() > 0
                        && entryFileName.equals(entry.getName())) {
                    listener.handleInputStream(new SevenZInputStream(sevenZFile),
                            entry.getSize());
                    return;
                }
            }
        }
        throwFileNotFound(archiveFile, entryFileName);
    }

    private static void getRarInputStream(final File archiveFile,
                                          final String entryFileName, final InputStreamListener listener,
                                          final Set<String> validExtensions) throws Throwable {
        try (final Archive archive = new Archive(archiveFile)) {
            for (final FileHeader fileHeader : archive.getFileHeaders()) {
                if (!fileHeader.isDirectory() && (validExtensions == null
                        || validExtensions.contains(getFileExtension(fileHeader
                        .getFileNameString()))) && fileHeader.getFullUnpackSize() > 0
                        && entryFileName.equals(fileHeader.getFileNameString())) {
                    listener.handleInputStream(archive.getInputStream(fileHeader),
                            fileHeader.getFullUnpackSize());
                    return;
                }
            }
        }
        throwFileNotFound(archiveFile, entryFileName);
    }

    public static byte[] readArchiveEntry(final String archiveFileName,
                                          final ArchiveEntry entry) throws Throwable {
        final byte[] data = new byte[(int) entry.getSize()];
        readArchiveEntry(archiveFileName, entry, data);
        return data;
    }

    public static void readArchiveEntry(final String archiveFileName,
                                        final ArchiveEntry entry, final byte[] data) throws Throwable {
        readArchiveEntry(archiveFileName, entry, data, 0, data.length);
    }

    public static void readArchiveEntry(final String archiveFileName,
                                        final ArchiveEntry entry, final byte[] data, final int offset,
                                        final int length) throws Throwable {
        FileUtil.getArchiveInputStream(archiveFileName, entry.getName(),
                (in, size) -> {
                    try (final DataInputStream dis = new DataInputStream(in)) {
                        dis.readFully(data, offset, length);
                    }
                });
    }

    private static void throwFileNotFound(final File archiveFile,
                                          final String entryFileName) throws FileNotFoundException {
        throw new FileNotFoundException(String.format("%s <%s> not found.",
                archiveFile.getPath(), entryFileName));
    }

    public static List<ArchiveEntry> getArchiveEntries(final File archiveFile)
            throws Throwable {
        return getArchiveEntries(archiveFile.getPath());
    }

    public static List<ArchiveEntry> getArchiveEntries(
            final String archiveFileName) throws Throwable {
        return getArchiveEntries(archiveFileName, WITHIN_ARCHIVE_EXTENSIONS);
    }

    public static List<ArchiveEntry> getArchiveEntries(
            final String archiveFileName, final Set<String> validExtensions)
            throws Throwable {
        final String extension = getFileExtension(archiveFileName);
        if (extension == null) {
            return null;
        }
        switch (extension) {
            case FILE_EXTENSION_ZIP:
                return getZipEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_7Z:
                return get7ZipEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_RAR:
                return getRarEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_TAR:
                return getTarEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_TAR_BZ2:
            case FILE_EXTENSION_TB2:
            case FILE_EXTENSION_TBZ:
            case FILE_EXTENSION_TBZ2:
                return getTarBz2Entries(archiveFileName, validExtensions);
            case FILE_EXTENSION_TAR_GZ:
            case FILE_EXTENSION_TGZ:
                return getTarGzEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_TAR_LZMA:
            case FILE_EXTENSION_TLZ:
                return getTarLzmaEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_TAR_XZ:
            case FILE_EXTENSION_TXZ:
                return getTarXzEntries(archiveFileName, validExtensions);
            case FILE_EXTENSION_TAR_Z:
            case FILE_EXTENSION_TZ:
                return getTarZEntries(archiveFileName, validExtensions);
            default:
                return null;
        }
    }

    private static List<ArchiveEntry> getRarEntries(final String fileName,
                                                    final Set<String> validExtensions) throws Throwable {
        final List<ArchiveEntry> entries = new ArrayList<>();
        try (final Archive archive = new Archive(new File(fileName))) {
            for (final FileHeader fileHeader : archive.getFileHeaders()) {
                if (!fileHeader.isDirectory() && (validExtensions == null
                        || validExtensions.contains(getFileExtension(fileHeader
                        .getFileNameString()))) && fileHeader.getFullUnpackSize() > 0) {
                    entries.add(new ArchiveEntry(fileHeader.getFileNameString(),
                            fileHeader.getFullUnpackSize()));
                }
            }
        }
        Collections.sort(entries, ArchiveEntry.CASE_INSENSITIVE_ORDER);
        return entries;
    }

    private static List<ArchiveEntry> get7ZipEntries(final String fileName,
                                                     final Set<String> validExtensions) throws Throwable {
        final List<ArchiveEntry> entries = new ArrayList<>();
        try (final SevenZFile sevenZFile = new SevenZFile(new File(fileName))) {
            while (true) {
                final SevenZArchiveEntry entry = sevenZFile.getNextEntry();
                if (entry == null || entries.size() >= MAX_ARCHIVE_ENTRIES) {
                    break;
                }
                if (!entry.isDirectory() && (validExtensions == null
                        || validExtensions.contains(getFileExtension(entry.getName())))
                        && entry.getSize() > 0) {
                    entries.add(new ArchiveEntry(entry.getName(), entry.getSize()));
                }
            }
        }
        Collections.sort(entries, ArchiveEntry.CASE_INSENSITIVE_ORDER);
        return entries;
    }

    public static List<ArchiveEntry> getZipEntries(final String fileName,
                                                   final Set<String> validExtensions) throws Throwable {
        final List<ArchiveEntry> entries = new ArrayList<>();
        try (final ZipFile zipFile = new ZipFile(new File(fileName))) {
            for (final Enumeration<? extends ZipEntry> i = zipFile.entries();
                 i.hasMoreElements() && entries.size() < MAX_ARCHIVE_ENTRIES; ) {
                final ZipEntry entry = i.nextElement();
                if (!entry.isDirectory() && (validExtensions == null
                        || validExtensions.contains(getFileExtension(entry.getName())))
                        && entry.getSize() > 0) {
                    entries.add(new ArchiveEntry(entry.getName(), entry.getSize()));
                }
            }
        }
        Collections.sort(entries, ArchiveEntry.CASE_INSENSITIVE_ORDER);
        return entries;
    }

    public static List<ArchiveEntry> getTarEntries(final InputStream in,
                                                   final Set<String> validExtensions) throws Throwable {
        final TarArchiveInputStream tarInput = new TarArchiveInputStream(in);
        final List<ArchiveEntry> entries = new ArrayList<>();
        TarArchiveEntry entry;
        while ((entry = tarInput.getNextTarEntry()) != null) {
            if (!entry.isDirectory() && (validExtensions == null
                    || validExtensions.contains(getFileExtension(entry.getName())))
                    && entry.getSize() > 0) {
                entries.add(new ArchiveEntry(entry.getName(), entry.getSize()));
            }
        }
        Collections.sort(entries, ArchiveEntry.CASE_INSENSITIVE_ORDER);
        return entries;
    }

    public static List<ArchiveEntry> getTarEntries(final String fileName,
                                                   final Set<String> validExtensions) throws Throwable {
        return getTarEntries(new FileInputStream(fileName), validExtensions);
    }

    public static List<ArchiveEntry> getTarBz2Entries(final String fileName,
                                                      final Set<String> validExtensions) throws Throwable {
        return getTarEntries(new BZip2CompressorInputStream(
                new FileInputStream(fileName)), validExtensions);
    }

    public static List<ArchiveEntry> getTarGzEntries(final String fileName,
                                                     final Set<String> validExtensions) throws Throwable {
        return getTarEntries(new GzipCompressorInputStream(
                new FileInputStream(fileName)), validExtensions);
    }

    public static List<ArchiveEntry> getTarLzmaEntries(final String fileName,
                                                       final Set<String> validExtensions) throws Throwable {
        return getTarEntries(new LZMACompressorInputStream(
                new FileInputStream(fileName)), validExtensions);
    }

    public static List<ArchiveEntry> getTarXzEntries(final String fileName,
                                                     final Set<String> validExtensions) throws Throwable {
        return getTarEntries(new XZCompressorInputStream(
                new FileInputStream(fileName)), validExtensions);
    }

    public static List<ArchiveEntry> getTarZEntries(final String fileName,
                                                    final Set<String> validExtensions) throws Throwable {
        return getTarEntries(new ZCompressorInputStream(
                new FileInputStream(fileName)), validExtensions);
    }

    public static boolean isArchiveFile(final String fileName) {
        return ARCHIVE_EXTENSIONS.contains(getFileExtension(fileName));
    }

    public static int getFileExtensionIndex(final String fileName) {

        for (int i = COMPRESSED_TAR_EXTENSIONS.length - 1; i >= 0; i--) {
            if (fileName.endsWith(COMPRESSED_TAR_EXTENSIONS[i])) {
                return fileName.length() - COMPRESSED_TAR_EXTENSIONS[i].length();
            }
        }

        for (int i = fileName.length() - 1; i >= 0; i--) {
            final char c = fileName.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                if (c == '.') {
                    return i + 1;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static String getFileExtension(final File file) {
        final String fileName = file.getName();
        final int index = getFileExtensionIndex(fileName);
        if (index < 0) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ENGLISH);
    }

    public static String getFileExtension(final String fileName) {
        return isBlank(fileName) ? null : getFileExtension(new File(fileName));
    }

    public static String getDirectoryPath(final String fileName) {
        if (isBlank(fileName)) {
            return null;
        }
        if (isDirectory(fileName)) {
            return fileName;
        } else {
            return new File(fileName).getParent();
        }
    }

    public static int getFileType(final String fileName) {
        return getFileType(new File(fileName));
    }

    public static int getFileType(final File file) {
        if (file.exists() && file.isFile()) {
            try (final DataInputStream in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {
                return getFileType(in);
            } catch (Throwable t) {
            }
        }
        return UNKNOWN;
    }

    public static int getFileType(final DataInputStream in) throws Throwable {

        if (!in.markSupported()) {
            throw new IOException("Stream does not support mark.");
        }

        in.mark(256);
        try {
            final int[] header = new int[5];
            readByteArray(in, header, 0, header.length, true);

            for (int i = 0; i < HEADERS.length; i++) {
                if (compareStrings(HEADERS[i], header)) {
                    return FILE_TYPES[i];
                }
            }

            in.reset();
            in.skipBytes(1);
            if (FdsFile.DISK_VERIFICATION.equals(readString(in,
                    FdsFile.DISK_VERIFICATION.length()))) {
                return FDS;
            }
        } finally {
            in.reset();
        }

        return UNKNOWN;
    }

    public static synchronized URL getResourceAsURL(String fileName) {
        return FileUtil.class.getResource(fileName);
    }

    public static synchronized InputStream getResourceAsStream(String fileName) {
        return FileUtil.class.getResourceAsStream(fileName);
    }

    public static synchronized String getWorkingDirectory() {
        if (isBlank(workingDir)) {
            File file = null;
            try {
                file = new File(FileUtil.class.getProtectionDomain().getCodeSource()
                        .getLocation().toURI().getPath());
            } catch (Throwable t) {
                //t.printStackTrace();
            }
            if (file != null && file.exists() && file.isFile()
                    && file.getName().toLowerCase().endsWith(".jar")) {
                workingDir = file.getParent();
            } else {
                workingDir = Paths.get("").toAbsolutePath().toString();
            }
        }
        return workingDir;
    }

    public static String getWorkingDirectory(final String... appends) {
        final StringBuilder sb = new StringBuilder(getWorkingDirectory());
        for (final String append : appends) {
            sb.append(File.separator);
            sb.append(append);
        }
        return sb.toString();
    }

    public static File getFile(final String directory, final String fileName) {
        return getFile(new File(directory), fileName);
    }

    public static File getFile(File directory, final String fileName) {
        if (directory == null) {
            directory = new File(AppPrefs.getInstance().getPaths()
                    .getContentDirectory());
        }
        if (isBlank(fileName)) {
            return new File(directory.getPath());
        } else {
            return new File(directory.getPath() + File.separator + fileName);
        }
    }

    public static String getFileName(final String path) {
        if (path == null) {
            return null;
        } else {
            return new File(path).getName();
        }
    }

    public static String getFileNameWithoutExtension(final String path) {
        return removeExtension(getFileName(path));
    }

    public static String removeExtension(final String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = getFileExtensionIndex(fileName);
        if (index <= 0) {
            return "";
        } else {
            return fileName.substring(0, index - 1);
        }
    }

    public static boolean directoryExists(final String directory) {
        if (isBlank(directory)) {
            return false;
        }
        final File dir = new File(directory);
        return isDirectory(directory) && dir.exists();
    }

    public static File mkdir(final String directory) {
        if (isBlank(directory)) {
            return null;
        }
        final File dir = new File(directory);
        if (isDirectory(directory) && !dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File findExistingParent(final String path) {
        return findExistingParent(new File(path));
    }

    public static File findExistingParent(File file) {
        while (file != null) {
            if (file.exists()) {
                return file;
            }
            file = file.getParentFile();
        }
        return file;
    }

    public static int getSuggestedStartIndex(final String filePrefix,
                                             final String outputDir) {

        final File outDir = new File(outputDir);
        if (!(outDir.exists() && outDir.isDirectory())) {
            return 0;
        }

        int index = -1;
        for (File file : outDir.listFiles()) {
            if (file.isFile()) {
                final String name = getFileNameWithoutExtension(file.getPath());
                if (isBlank(name) || name.length() < 4
                        || !name.startsWith(filePrefix)) {
                    continue;
                }
                final int i = name.lastIndexOf('-');
                if (i < 0 || i == name.length() - 1) {
                    continue;
                }
                try {
                    index = Math.max(Integer.parseInt(name.substring(i + 1)), index);
                } catch (Throwable t) {
                }
            }
        }
        return index + 1;
    }

    public static int indexOf(final List<String> directories,
                              final String directory) {
        if (directories == null || isBlank(directory)) {
            return -1;
        }
        final File dir = new File(directory);
        for (int i = directories.size() - 1; i >= 0; i--) {
            final String dirName = directories.get(i);
            if (dirName == null) {
                continue;
            }
            if (new File(dirName).equals(dir)) {
                return i;
            }
        }
        return -1;
    }

    public static String appendSeparator(final String path) {
        if (path.endsWith(File.separator) || path.endsWith("/")
                || path.endsWith("\\")) {
            return path;
        } else {
            return path + File.separator;
        }
    }

    public static String getCanonicalName(final String name) {
        if (isBlank(name)) {
            return null;
        }
        try {
            return new File(name).getCanonicalPath();
        } catch (Throwable t) {
            return name.toLowerCase(Locale.ENGLISH);
        }
    }

    public static File createUniqueFile(final String filePath,
                                        final boolean alwaysAppendSuffix, String suffix) {
        return createUniqueFile(new File(filePath), alwaysAppendSuffix, suffix);
    }

    public static File createUniqueFile(final File file,
                                        final boolean alwaysAppendSuffix, String suffix) {

        final String fileName = file.getName();
        final int index = getFileExtensionIndex(fileName);
        if (index <= 0) {
            return null;
        }
        final String extension = fileName.substring(index).toLowerCase(
                Locale.ENGLISH);
        final String prefix = fileName.substring(0, index - 1);
        String directory = file.getParent();
        if (directory == null) {
            directory = "";
        }

        return createUniqueFile(directory, prefix, extension, alwaysAppendSuffix,
                suffix);
    }

    public static File createUniqueFile(final String directory,
                                        final String prefix, final String extension,
                                        final boolean alwaysAppendSuffix, String suffix) {

        return new File(appendSeparator(directory) + createUniqueFileName(directory,
                prefix, extension, alwaysAppendSuffix, suffix));
    }

    public static String createUniqueFileName(final String directory,
                                              final String prefix, final String extension,
                                              final boolean alwaysAppendSuffix, String suffix) {

        final Set<String> nameSet = new HashSet<>();
        for (final File file : new File(directory).listFiles()) {
            final String name = file.getName();
            if (name.startsWith(prefix)) {
                nameSet.add(name);
            }
        }

        if (isBlank(suffix)) {
            suffix = "-%03d";
        }
        String fileName = prefix + "." + extension;
        if (alwaysAppendSuffix || nameSet.contains(fileName)) {
            for (int i = alwaysAppendSuffix ? 0 : 1; i < MAX_FILE_NUMBER; i++) {
                fileName = prefix + String.format(suffix, i) + "." + extension;
                if (!nameSet.contains(fileName)) {
                    break;
                }
            }
        }

        return fileName;
    }

    public static boolean containsSeparators(final String path) {
        return path.contains("/") || path.contains("\\") || path.contains(":")
                || path.contains(";");
    }

    public static boolean isDirectory(final String path) {
        final File dir = new File(path);
        return dir.isDirectory() || isDirectoryPath(dir, path);
    }

    public static boolean isDirectory(final File file) {
        if (file.isDirectory()) {
            return true;
        }
        return isDirectoryPath(file, file.getPath());
    }

    private static boolean isDirectoryPath(final File dir, String path) {
        path = path.trim();
        return path.endsWith("/") || path.endsWith("\\")
                || path.endsWith(":") || getFileExtensionIndex(dir.getPath()) < 0;
    }

    public static String createSaveFile(final String fileName) {
        return appendSeparator(AppPrefs.getInstance().getPaths().getSaveStatesDir())
                + getFileNameWithoutExtension(fileName) + ".save";
    }

    public static String createGamePreferencesFile(String fileName) {
        return appendSeparator(AppPrefs.getInstance().getPaths()
                .getGamePreferencesDir()) + getFileNameWithoutExtension(fileName)
                + ".preferences";
    }

    public static String createLogFile() {
        final String entryFileName = App.getEntryFileName();
        return appendSeparator(AppPrefs.getInstance().getPaths().getLogsDir())
                + (isBlank(entryFileName) ? "trace"
                : getFileNameWithoutExtension(entryFileName)) + ".log";
    }

    public static String createCheatFile(String fileName) {
        return appendSeparator(AppPrefs.getInstance().getPaths()
                .getCheatsDir()) + getFileNameWithoutExtension(fileName) + ".cht";
    }

    public static synchronized String getFileTimestamp(final int index,
                                                       final Date date) {
        return String.format("%d  %s      ", index, dateFormat.format(date));
    }

    public static String getFileTimestamp(final int index, final File file) {
        return getFileTimestamp(index, new Date(file.lastModified()));
    }

    public static boolean isFamicomDiskSystemFile(final FilePath filePath) {
        return isFamicomDiskSystemFile(filePath.isArchivedEntry()
                ? filePath.getEntryPath() : filePath.getArchivePath());
    }

    public static boolean isFamicomDiskSystemFile(final String fileName) {
        return "fds".equals(getFileExtension(fileName));
    }

    public static int getDefaultArchiveEntry(final String archiveFileName,
                                             final List<String> entries) {
        return getDefaultArchiveEntry(archiveFileName, entries, EntryRegion
                .getPrioritizedRegions(AppPrefs.getInstance().getArchivePrefs()
                        .getArchiveEntryRegion()));
    }

    private static int getDefaultArchiveEntry(final String archiveFileName,
                                              final List<String> entries, final String[] regions) {

        if (entries.isEmpty()) {
            return -1;
        } else if (entries.size() == 1) {
            return 0;
        }

        String fileName = getFileNameWithoutExtension(archiveFileName);
        int index = fileName.indexOf('(');
        if (index >= 0) {
            fileName = fileName.substring(0, index);
        }
        index = fileName.indexOf('[');
        if (index >= 0) {
            fileName = fileName.substring(0, index);
        }
        fileName = fileName.trim().toLowerCase(Locale.ENGLISH);

        int bestIndex = -1;
        int bestRank = -1;
        for (int i = entries.size() - 1; i >= 0; i--) {
            final int rank = rankArchiveEntry(fileName, entries.get(i), regions);
            if (rank > bestRank) {
                bestRank = rank;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static int rankArchiveEntry(String archiveFileName, String entry,
                                        final String[] regions) {
        entry = entry.toLowerCase(Locale.ENGLISH);
        if (entry.endsWith(".nsf") || entry.endsWith(".nsfe")) {
            return -1;
        }
        int rank = 0;
        final int len2 = regions.length * regions.length;
        final int len3 = len2 * regions.length;
        final int len4 = len3 * regions.length;
        final int len5 = len4 * regions.length;
        if (GoodDumpPattern.matcher(entry).find()) {
            rank += len5;
        } else if (entry.indexOf('[') < 0 || entry.contains("ntsc")) {
            rank += len4;
        }
        for (int i = regions.length - 1; i >= 0; i--) {
            if (entry.contains(regions[i])) {
                rank += len3 * i;
                break;
            }
        }
        if (entry.contains(archiveFileName)) {
            rank += len2;
        }
        final Matcher matcher = REVISION_PATTERN.matcher(entry);
        if (matcher.find()) {
            rank += regions.length * min(9, Integer.valueOf(matcher.group(2)));
        }
        return rank - entry.length();
    }

    public interface InputStreamListener {
        void handleInputStream(InputStream in, long fileSize) throws Throwable;
    }

    private static class SevenZInputStream extends InputStream {

        final SevenZFile sevenZFile;

        public SevenZInputStream(SevenZFile sevenZFile) {
            this.sevenZFile = sevenZFile;
        }

        @Override
        public int read() throws IOException {
            return sevenZFile.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return sevenZFile.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return sevenZFile.read(b, off, len);
        }
    }
}
