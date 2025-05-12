package cn.kinlon.emu.gui.fds;

import cn.kinlon.emu.files.FilePath;
import cn.kinlon.emu.preferences.AppPrefs;

import java.io.Serializable;

import static cn.kinlon.emu.gui.fds.DiskActivityIndicator.SCROLL_LOCK;

public class FamicomDiskSystemPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private FilePath biosFile;
    private DiskActivityIndicator diskActivityIndicator;
    private Boolean fastForwardDuringDiskAccess;

    public FilePath getBiosFile() {
        synchronized (AppPrefs.class) {
            return biosFile;
        }
    }

    public void setBiosFile(final FilePath biosFile) {
        synchronized (AppPrefs.class) {
            this.biosFile = biosFile;
        }
    }

    public DiskActivityIndicator getDiskActivityIndicator() {
        synchronized (AppPrefs.class) {
            if (diskActivityIndicator == null) {
                diskActivityIndicator = SCROLL_LOCK;
            }
            return diskActivityIndicator;
        }
    }

    public void setDiskActivityIndicator(
            final DiskActivityIndicator diskActivityIndicator) {
        synchronized (AppPrefs.class) {
            this.diskActivityIndicator = diskActivityIndicator;
        }
    }

    public boolean isFastForwardDuringDiskAccess() {
        synchronized (AppPrefs.class) {
            if (fastForwardDuringDiskAccess == null) {
                fastForwardDuringDiskAccess = true;
            }
            return fastForwardDuringDiskAccess;
        }
    }

    public void setFastForwardDuringDiskAccess(
            final boolean fastForwardDuringDiskAccess) {
        synchronized (AppPrefs.class) {
            this.fastForwardDuringDiskAccess = fastForwardDuringDiskAccess;
        }
    }
}