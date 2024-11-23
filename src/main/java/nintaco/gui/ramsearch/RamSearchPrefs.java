package nintaco.gui.ramsearch;

import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class RamSearchPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer aValue;
    private Integer bValue;
    private Integer filterIndex;
    private Integer wordSizeIndex;
    private Integer alignIndex;
    private Integer valueFormat;
    private Integer memoryRange;
    private Integer saveSlotIndex;
    private Boolean autofilter;

    public Integer getAValue() {
        return aValue;
    }

    public void setAValue(final Integer aValue) {
        synchronized (AppPrefs.class) {
            this.aValue = aValue;
        }
    }

    public Integer getBValue() {
        synchronized (AppPrefs.class) {
            return bValue;
        }
    }

    public void setBValue(final Integer bValue) {
        synchronized (AppPrefs.class) {
            this.bValue = bValue;
        }
    }

    public int getSaveSlotIndex() {
        synchronized (AppPrefs.class) {
            if (saveSlotIndex == null) {
                saveSlotIndex = 0;
            }
            return saveSlotIndex;
        }
    }

    public void setSaveSlotIndex(final int saveSlotIndex) {
        synchronized (AppPrefs.class) {
            this.saveSlotIndex = saveSlotIndex;
        }
    }

    public int getFilterIndex() {
        synchronized (AppPrefs.class) {
            if (filterIndex == null) {
                filterIndex = 0;
            }
            return filterIndex;
        }
    }

    public void setFilterIndex(final int filterIndex) {
        synchronized (AppPrefs.class) {
            this.filterIndex = filterIndex;
        }
    }

    public int getWordSizeIndex() {
        synchronized (AppPrefs.class) {
            if (wordSizeIndex == null) {
                wordSizeIndex = 0;
            }
            return wordSizeIndex;
        }
    }

    public void setWordSizeIndex(final int wordSizeIndex) {
        synchronized (AppPrefs.class) {
            this.wordSizeIndex = wordSizeIndex;
        }
    }

    public int getAlignIndex() {
        synchronized (AppPrefs.class) {
            if (alignIndex == null) {
                alignIndex = 0;
            }
            return alignIndex;
        }
    }

    public void setAlignIndex(final int alignIndex) {
        synchronized (AppPrefs.class) {
            this.alignIndex = alignIndex;
        }
    }

    public int getValueFormat() {
        synchronized (AppPrefs.class) {
            if (valueFormat == null) {
                valueFormat = 1;
            }
            return valueFormat;
        }
    }

    public void setValueFormat(final int valueFormat) {
        synchronized (AppPrefs.class) {
            this.valueFormat = valueFormat;
        }
    }

    public int getMemoryRange() {
        synchronized (AppPrefs.class) {
            if (memoryRange == null) {
                memoryRange = 0;
            }
            return memoryRange;
        }
    }

    public void setMemoryRange(final int memoryRange) {
        synchronized (AppPrefs.class) {
            this.memoryRange = memoryRange;
        }
    }

    public boolean getAutofilter() {
        synchronized (AppPrefs.class) {
            if (autofilter == null) {
                autofilter = false;
            }
            return autofilter;
        }
    }

    public void setAutofilter(final boolean autofilter) {
        synchronized (AppPrefs.class) {
            this.autofilter = autofilter;
        }
    }
}
