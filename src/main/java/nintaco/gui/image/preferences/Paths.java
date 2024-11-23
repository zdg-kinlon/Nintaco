package nintaco.gui.image.preferences;

import nintaco.files.FilePath;
import nintaco.preferences.AppPrefs;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.*;
import static nintaco.util.StringUtil.isBlank;

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

    public boolean isUseLastVisitedFilesDir() {
        synchronized (AppPrefs.class) {
            return !doNotUseLastVisitedFilesDir;
        }
    }

    public void setUseLastVisitedFilesDir(final boolean useLastVisitedFilesDir) {
        synchronized (AppPrefs.class) {
            this.doNotUseLastVisitedFilesDir = !useLastVisitedFilesDir;
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

    public void setSaveStatesDir(final String saveStatesDir) {
        synchronized (AppPrefs.class) {
            this.saveStatesDir = saveStatesDir;
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

    public void setGamePreferencesDir(final String gamePreferencesDir) {
        synchronized (AppPrefs.class) {
            this.gamePreferencesDir = gamePreferencesDir;
        }
    }

    public String getSaveEditedNesFileDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(saveEditedNesFileDir)) {
                saveEditedNesFileDir = getMostRecentDirectory();
            }
            return saveEditedNesFileDir;
        }
    }

    public void setSaveEditedNesFileDir(final String saveEditedNesFileDir) {
        synchronized (AppPrefs.class) {
            this.saveEditedNesFileDir = saveEditedNesFileDir;
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

    public String getWatchesDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(watchesDir)) {
                watchesDir = getContentDirectory("watches");
            }
            return watchesDir;
        }
    }

    public void setWatchesDir(final String recentWatchesDir) {
        synchronized (AppPrefs.class) {
            this.watchesDir = recentWatchesDir;
        }
    }

    public String getLogsDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(logsDir)) {
                logsDir = getContentDirectory("logs");
            }
            return logsDir;
        }
    }

    public void setLogsDir(final String logsDir) {
        synchronized (AppPrefs.class) {
            this.logsDir = logsDir;
        }
    }

    public String getHistoriesDir() {
        synchronized (AppPrefs.class) {
            if (historiesDir == null) {
                historiesDir = getContentDirectory("histories");
            }
            return historiesDir;
        }
    }

    public void setHistoriesDir(final String historiesDir) {
        synchronized (AppPrefs.class) {
            this.historiesDir = historiesDir;
        }
    }

    public String getJarDir() {
        synchronized (AppPrefs.class) {
            if (jarDir == null) {
                jarDir = getMostRecentDirectory();
            }
            return jarDir;
        }
    }

    public void setJarDir(final String jarDir) {
        synchronized (AppPrefs.class) {
            this.jarDir = jarDir;
        }
    }

    public String getMediaDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(mediaDir)) {
                mediaDir = getContentDirectory("media");
            }
            return mediaDir;
        }
    }

    public void setMediaDir(final String mediaDir) {
        synchronized (AppPrefs.class) {
            this.mediaDir = mediaDir;
        }
    }

    public String getPalettesDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(palettesDir)) {
                palettesDir = getContentDirectory("palettes");
            }
            return palettesDir;
        }
    }

    public void setPalettesDir(final String palettesDir) {
        synchronized (AppPrefs.class) {
            this.palettesDir = palettesDir;
        }
    }

    public String getPatchesDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(patchesDir)) {
                patchesDir = getContentDirectory("patches");
            }
            return patchesDir;
        }
    }

    public void setPatchesDir(final String patchesDir) {
        synchronized (AppPrefs.class) {
            this.patchesDir = patchesDir;
        }
    }

    public String getBreakpointsDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(breakpointsDir)) {
                breakpointsDir = getContentDirectory("debug", "breakpoints");
            }
            return breakpointsDir;
        }
    }

    public void setBreakpointsDir(final String breakpointsDir) {
        synchronized (AppPrefs.class) {
            this.breakpointsDir = breakpointsDir;
        }
    }

    public String getAddressLabelsDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(addressLabelsDir)) {
                addressLabelsDir = getContentDirectory("debug", "labels");
            }
            return addressLabelsDir;
        }
    }

    public void setAddressLabelsDir(final String addressLabelsDir) {
        synchronized (AppPrefs.class) {
            this.addressLabelsDir = addressLabelsDir;
        }
    }

    public String getMapsDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(mapsDir)) {
                mapsDir = getContentDirectory("maps");
            }
            return mapsDir;
        }
    }

    public void setMapsDir(final String mapsDir) {
        synchronized (AppPrefs.class) {
            this.mapsDir = mapsDir;
        }
    }

    public String getSpritesDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(spritesDir)) {
                spritesDir = getContentDirectory("sprites");
            }
            return spritesDir;
        }
    }

    public void setSpritesDir(final String spritesDir) {
        synchronized (AppPrefs.class) {
            this.spritesDir = spritesDir;
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

    public void setCheatsDir(final String cheatsDir) {
        synchronized (AppPrefs.class) {
            this.cheatsDir = cheatsDir;
        }
    }

    public String getBasicDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(basicDir)) {
                basicDir = getMostRecentDirectory();
            }
            return basicDir;
        }
    }

    public void setBasicDir(final String basicDir) {
        synchronized (AppPrefs.class) {
            this.basicDir = basicDir;
        }
    }

    public String getBackgroundDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(backgroundDir)) {
                backgroundDir = getMostRecentDirectory();
            }
            return backgroundDir;
        }
    }

    public void setBackgroundDir(final String backgroundDir) {
        synchronized (AppPrefs.class) {
            this.backgroundDir = backgroundDir;
        }
    }

    public String getTapeDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(tapeDir)) {
                tapeDir = getMostRecentDirectory();
            }
            return tapeDir;
        }
    }

    public void setTapeDir(final String tapeDir) {
        synchronized (AppPrefs.class) {
            this.tapeDir = tapeDir;
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

    public String getScreenshotsDir() {
        synchronized (AppPrefs.class) {
            if (isBlank(screenshotsDir)) {
                screenshotsDir = getContentDirectory("screenshots");
            }
            return screenshotsDir;
        }
    }

    public void setScreenshotsDir(final String screenshotsDir) {
        synchronized (AppPrefs.class) {
            this.screenshotsDir = screenshotsDir;
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

    public List<String> getRecentHistoryProjects() {
        synchronized (AppPrefs.class) {
            initRecentHistoryProjects();
            return recentHistoryProjects;
        }
    }

    public void clearRecentFiles() {
        synchronized (AppPrefs.class) {
            initRecentFiles();
            recentFiles.clear();
            lockRecentFiles = false;
        }
    }

    public void clearRecentArchives() {
        synchronized (AppPrefs.class) {
            initRecentArchives();
            recentArchives.clear();
            lockRecentArchives = false;
        }
    }

    public void clearRecentHistoryProjects() {
        synchronized (AppPrefs.class) {
            initRecentHistoryProjects();
            recentHistoryProjects.clear();
            lockRecentHistoryProjects = false;
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

    public void addRecentHistoryProject(final String fileName) {
        synchronized (AppPrefs.class) {
            if (!lockRecentHistoryProjects) {
                initRecentHistoryProjects();
                int index = recentHistoryProjects.indexOf(fileName);
                if (index != 0) {
                    if (index < 0) {
                        while (recentHistoryProjects.size() > 9) {
                            recentHistoryProjects.remove(recentHistoryProjects.size() - 1);
                        }
                    } else {
                        recentHistoryProjects.remove(index);
                    }
                    recentHistoryProjects.add(0, fileName);
                }
            }
            addRecentDirectory(getDirectoryPath(fileName));
        }
    }

    public boolean isLockRecentFiles() {
        synchronized (AppPrefs.class) {
            return lockRecentFiles;
        }
    }

    public void setLockRecentFiles(final boolean lockRecentFiles) {
        synchronized (AppPrefs.class) {
            this.lockRecentFiles = lockRecentFiles;
        }
    }

    public boolean isLockRecentArchives() {
        synchronized (AppPrefs.class) {
            return lockRecentArchives;
        }
    }

    public void setLockRecentArchives(final boolean lockRecentArchives) {
        synchronized (AppPrefs.class) {
            this.lockRecentArchives = lockRecentArchives;
        }
    }

    public boolean isLockRecentHistoryProjects() {
        synchronized (AppPrefs.class) {
            return lockRecentHistoryProjects;
        }
    }

    public void setLockRecentHistoryProjects(
            final boolean lockRecentHistoryProjects) {
        synchronized (AppPrefs.class) {
            this.lockRecentHistoryProjects = lockRecentHistoryProjects;
        }
    }

    public List<String> getRecentDirectories() {
        synchronized (AppPrefs.class) {
            initRecentDirectories();
            return recentDirectories;
        }
    }

    public void clearRecentDirectories() {
        synchronized (AppPrefs.class) {
            initRecentDirectories();
            recentDirectories.clear();
            lockRecentDirectories = false;
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

    public boolean isLockRecentDirectories() {
        synchronized (AppPrefs.class) {
            return lockRecentDirectories;
        }
    }

    public void setLockRecentDirectories(final boolean lockRecentDirectories) {
        synchronized (AppPrefs.class) {
            this.lockRecentDirectories = lockRecentDirectories;
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
