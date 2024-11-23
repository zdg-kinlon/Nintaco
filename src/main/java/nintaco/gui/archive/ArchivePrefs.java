package nintaco.gui.archive;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class ArchivePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private EntryRegion archiveEntryRegion;
    private Boolean openDefaultArchiveEntry;

    public EntryRegion getArchiveEntryRegion() {
        synchronized (AppPrefs.class) {
            if (archiveEntryRegion == null) {
                archiveEntryRegion = EntryRegion.USA;
            }
            return archiveEntryRegion;
        }
    }

    public void setArchiveEntryRegion(final EntryRegion archiveEntryRegion) {
        synchronized (AppPrefs.class) {
            this.archiveEntryRegion = archiveEntryRegion;
        }
    }

    public boolean isOpenDefaultArchiveEntry() {
        synchronized (AppPrefs.class) {
            if (openDefaultArchiveEntry == null) {
                openDefaultArchiveEntry = false;
            }
            return openDefaultArchiveEntry;
        }
    }

    public void setOpenDefaultArchiveEntry(
            final boolean openDefaultArchiveEntry) {
        synchronized (AppPrefs.class) {
            this.openDefaultArchiveEntry = openDefaultArchiveEntry;
        }
    }
}
