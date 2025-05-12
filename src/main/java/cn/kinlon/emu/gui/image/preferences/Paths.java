package cn.kinlon.emu.gui.image.preferences;

import cn.kinlon.emu.files.FilePath;
import cn.kinlon.emu.preferences.AppPrefs;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static cn.kinlon.emu.files.FileUtil.*;
import static cn.kinlon.emu.utils.StringUtil.isBlank;

public class Paths implements Serializable {

    private static final long serialVersionUID = 0;

    private List<FilePath> recentFiles;
    private List<String> recentArchives;
    private List<String> recentHistoryProjects;
    private List<String> recentDirectories;
    private boolean doNotUseLastVisitedFilesDir;
    private int fileExtensionFilterIndex;
    private boolean lockRecentFiles;
    private boolean lockRecentArchives;
    private boolean lockRecentHistoryProjects;
    private boolean lockRecentDirectories;

    private String filesDir;
    private String contentDir;

    private String addressLabelsDir;
    private String basicDir;
    private String backgroundDir;
    private String breakpointsDir;
    private String cheatsDir;
    private String fdsBiosDir;
    private String gamePreferencesDir;
    private String historiesDir;
    private String jarDir;
    private String loadCharacterTableDir;
    private String logsDir;
    private String mapsDir;
    private String mediaDir;
    private String palettesDir;
    private String patchesDir;
    private String saveStatesDir;
    private String screenshotsDir;
    private String saveEditedNesFileDir;
    private String spritesDir;
    private String tapeDir;
    private String watchesDir;

    public void init() {
        getContentDirectory();
    }

    private void initRecentFiles() {
        synchronized (AppPrefs.class) {
            if (recentFiles == null) {
                recentFiles = new ArrayList<>();
            }
        }
    }

    private void initRecentArchives() {
        synchronized (AppPrefs.class) {
            if (recentArchives == null) {
                recentArchives = new ArrayList<>();
            }
        }
    }

    private void initRecentHistoryProjects() {
        synchronized (AppPrefs.class) {
            if (recentHistoryProjects == null) {
                recentHistoryProjects = new ArrayList<>();
            }
        }
    }

    private void initRecentDirectories() {
        synchronized (AppPrefs.class) {
            if (recentDirectories == null) {
                recentDirectories = new ArrayList<>();
            }
        }
    }

    public String getContentDirectory() {
        synchronized (AppPrefs.class) {
            if (!directoryExists(contentDir)) {
                setContentDirectory(getWorkingDirectory());
            }
            return contentDir;
        }
    }

    public void setContentDirectory(final String contentDir) {
        synchronized (AppPrefs.class) {
            this.contentDir = contentDir;

            addressLabelsDir = null;
            basicDir = null;
            backgroundDir = null;
            breakpointsDir = null;
            cheatsDir = null;
            fdsBiosDir = null;
            gamePreferencesDir = null;
            historiesDir = null;
            jarDir = null;
            loadCharacterTableDir = null;
            logsDir = null;
            mapsDir = null;
            mediaDir = null;
            palettesDir = null;
            patchesDir = null;
            saveStatesDir = null;
            screenshotsDir = null;
            saveEditedNesFileDir = null;
            spritesDir = null;
            tapeDir = null;
            watchesDir = null;
        }
    }

    public String getContentDirectory(final String... appends) {
        final StringBuilder sb = new StringBuilder(getContentDirectory());
        for (final String append : appends) {
            sb.append(File.separator);
            sb.append(append);
        }
        return sb.toString();
    }

    public String getFilesDir() {
        synchronized (AppPrefs.class) {
            if (filesDir == null) {
                filesDir = getContentDirectory();
            }
            return filesDir;
        }
    }

    public void setFilesDir(final String filesDir) {
        synchronized (AppPrefs.class) {
            this.filesDir = filesDir;
        }
    }

    public String getSaveStatesDir() {
        synchronized (AppPrefs.class) {
            if (saveStatesDir == null) {
                saveStatesDir = getContentDirectory("states");
            }
            return saveStatesDir;
        }
    }

    public String getGamePreferencesDir() {
        synchronized (AppPrefs.class) {
            if (gamePreferencesDir == null) {
                gamePreferencesDir = getContentDirectory("preferences");
            }
            return gamePreferencesDir;
        }
    }

    public String getLoadCharacterTableDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(loadCharacterTableDir)) {
                loadCharacterTableDir = getMostRecentDirectory();
            }
            return loadCharacterTableDir;
        }
    }

    public void setLoadCharacterTableDir(
            final String loadCharacterTableDir) {
        synchronized (AppPrefs.class) {
            this.loadCharacterTableDir = loadCharacterTableDir;
        }
    }

    public String getCheatsDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(cheatsDir)) {
                cheatsDir = getContentDirectory("cheats");
            }
            return cheatsDir;
        }
    }

    public String getFdsBiosDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(fdsBiosDir)) {
                fdsBiosDir = getMostRecentDirectory();
            }
            return fdsBiosDir;
        }
    }

    public void setFdsBiosDir(final String fdsBiosDir) {
        synchronized (AppPrefs.class) {
            this.fdsBiosDir = fdsBiosDir;
        }
    }

    public int getFileExtensionFilterIndex() {
        synchronized (AppPrefs.class) {
            return fileExtensionFilterIndex;
        }
    }

    public void setFileExtensionFilterIndex(final int imageExtensionFilterIndex) {
        synchronized (AppPrefs.class) {
            this.fileExtensionFilterIndex = imageExtensionFilterIndex;
        }
    }

    public List<FilePath> getRecentFiles() {
        synchronized (AppPrefs.class) {
            initRecentFiles();
            return recentFiles;
        }
    }

    public List<String> getRecentArchives() {
        synchronized (AppPrefs.class) {
            initRecentArchives();
            return recentArchives;
        }
    }

    public void addRecentFile(final String entryPath) {
        addRecentFile(entryPath, null);
    }

    public void addRecentFile(final String entryPath, final String archivePath) {
        if (isBlank(entryPath)) {
            addRecentArchiveFile(archivePath);
        } else {
            synchronized (AppPrefs.class) {
                if (!lockRecentFiles) {
                    initRecentFiles();
                    final FilePath filePath = new FilePath(entryPath, archivePath);
                    int index = recentFiles.indexOf(filePath);
                    if (index != 0) {
                        if (index < 0) {
                            while (recentFiles.size() > 9) {
                                recentFiles.remove(recentFiles.size() - 1);
                            }
                        } else {
                            recentFiles.remove(index);
                        }
                        recentFiles.add(0, filePath);
                    }
                    if (!isBlank(archivePath)) {
                        addRecentArchive(filePath.getArchivePath());
                    }
                } else if (!isBlank(archivePath)) {
                    addRecentArchive(getCanonicalName(archivePath));
                }
                if (archivePath == null) {
                    addRecentDirectory(getDirectoryPath(entryPath));
                } else {
                    addRecentDirectory(getDirectoryPath(archivePath));
                }
            }
        }
    }

    public void addRecentArchiveFile(final String archivePath) {
        addRecentArchive(getCanonicalName(archivePath));
        addRecentDirectory(getDirectoryPath(archivePath));
    }

    private void addRecentArchive(final String archivePath) {
        synchronized (AppPrefs.class) {
            if (!lockRecentArchives) {
                initRecentArchives();
                int index = recentArchives.indexOf(archivePath);
                if (index != 0) {
                    if (index < 0) {
                        while (recentArchives.size() > 9) {
                            recentArchives.remove(recentArchives.size() - 1);
                        }
                    } else {
                        recentArchives.remove(index);
                    }
                    recentArchives.add(0, archivePath);
                }
            }
        }
    }

    public boolean isLockRecentFiles() {
        synchronized (AppPrefs.class) {
            return lockRecentFiles;
        }
    }

    public boolean isLockRecentArchives() {
        synchronized (AppPrefs.class) {
            return lockRecentArchives;
        }
    }

    public List<String> getRecentDirectories() {
        synchronized (AppPrefs.class) {
            initRecentDirectories();
            return recentDirectories;
        }
    }

    public void addRecentDirectory(String directory) {
        synchronized (AppPrefs.class) {
            if (!lockRecentDirectories) {
                initRecentDirectories();
                directory = getCanonicalName(directory);
                final int index = recentDirectories.indexOf(directory);
                if (index != 0) {
                    if (index < 0) {
                        while (recentDirectories.size() > 9) {
                            recentDirectories.remove(recentDirectories.size() - 1);
                        }
                    } else {
                        recentDirectories.remove(index);
                    }
                    recentDirectories.add(0, directory);
                }
            }
        }
    }

    public String getMostRecentDirectory() {
        synchronized (AppPrefs.class) {
            initRecentDirectories();
            return (recentDirectories.isEmpty() || doNotUseLastVisitedFilesDir)
                    ? getFilesDir() : recentDirectories.get(0);
        }
    }
}
