package cn.kinlon.emu.files;

import java.io.Serializable;
import java.util.Objects;

import static cn.kinlon.emu.files.FileUtil.getCanonicalName;
import static cn.kinlon.emu.files.FileUtil.getFileName;
import static cn.kinlon.emu.utils.StringUtil.isBlank;

public class FilePath implements Serializable {

    private static final long serialVersionUID = 0;
    private final String archivePath;
    private final String entryPath;
    public FilePath(final String entryPath) {
        this(entryPath, null);
    }

    public FilePath(final String entryPath, final String archivePath) {
        this.entryPath = isBlank(archivePath) ? getCanonicalName(entryPath)
                : entryPath;
        this.archivePath = getCanonicalName(archivePath);
    }

    public static FilePath fromLongString(final String longString) {
        final int lt = longString.indexOf('<');
        if (lt < 2) {
            return new FilePath(longString);
        }
        final int gt = longString.lastIndexOf('>');
        if (gt - lt < 2) {
            return null;
        }
        return new FilePath(longString.substring(lt + 1, gt).trim(),
                longString.substring(0, lt).trim());
    }

    public String getOuterPath() {
        return (archivePath == null) ? entryPath : archivePath;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public String getArchiveFileName() {
        return getFileName(archivePath);
    }

    public String getEntryFileName() {
        return getFileName(entryPath);
    }

    public boolean isArchivedEntry() {
        return !isBlank(archivePath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entryPath) ^ Objects.hashCode(archivePath);
    }

    @Override
    public boolean equals(final Object obj) {
        final FilePath other = (FilePath) obj;
        return Objects.equals(archivePath, other.archivePath)
                && Objects.equals(entryPath, other.entryPath);
    }

    public String toLongString() {
        if (archivePath == null) {
            return entryPath;
        } else {
            return String.format("%s <%s>", archivePath, entryPath);
        }
    }

    @Override
    public String toString() {
        if (archivePath == null) {
            return getEntryFileName();
        } else {
            return String.format("%s <%s>", getArchiveFileName(), getEntryFileName());
        }
    }
}
