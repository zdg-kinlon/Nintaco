package cn.kinlon.emu.files;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import cn.kinlon.emu.preferences.AppPrefs;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static cn.kinlon.emu.files.FileType.*;
import static cn.kinlon.emu.utils.StreamUtil.readByteArray;
import static cn.kinlon.emu.utils.StreamUtil.readString;
import static cn.kinlon.emu.utils.StringUtil.compareStrings;
import static cn.kinlon.emu.utils.StringUtil.isBlank;

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

    private static void throwFileNotFound(final File archiveFile,
                                          final String entryFileName) throws FileNotFoundException {
        throw new FileNotFoundException(java.lang.String.format("%s <%s> not found.",
                archiveFile.getPath(), entryFileName));
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

    public static String createCheatFile(String fileName) {
        return appendSeparator(AppPrefs.getInstance().getPaths()
                .getCheatsDir()) + getFileNameWithoutExtension(fileName) + ".cht";
    }

    public static boolean isFamicomDiskSystemFile(final String fileName) {
        return "fds".equals(getFileExtension(fileName));
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
