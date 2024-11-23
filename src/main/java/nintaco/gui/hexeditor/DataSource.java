package nintaco.gui.hexeditor;

import java.util.ArrayList;
import java.util.List;

public abstract class DataSource {

    public static final int CpuMemory = 0;
    public static final int PpuMemory = 1;
    public static final int FileContents = 2;
    final int[] cache;
    private final List<Edit> edits = new ArrayList<>();
    private int editIndex;
    private int startSelectedAddress;
    private int endSelectedAddress;
    private int scrollY;

    public DataSource(final int size) {
        this(new int[size]);
    }

    public DataSource(final int[] cache) {
        this.cache = cache;
    }

    public abstract int peek(int address);

    public abstract void write(int address, int value);

    public abstract int getIndex();

    public void writeCache(final int address, final int value) {
        if (address >= 0 && address < cache.length) {
            cache[address] = value;
        }
    }

    public int readColored(final int address) {
        return (readCache(address) & 0xFF00) | (peek(address) & 0x00FF);
    }

    public int readCache(final int address) {
        return address < 0 || address >= cache.length ? 0 : cache[address];
    }

    public void refreshCache() {
        for (int i = cache.length - 1; i >= 0; i--) {
            final int peekValue = peek(i);
            final int cacheValue = cache[i] & 0xFF;
            int cacheColor = (cache[i] >> 8) & 0xFF;
            if (peekValue != cacheValue) {
                cacheColor = 0;
            }
            cache[i] = (cacheColor << 8) | peekValue;
        }
    }

    public int[] getCache() {
        return cache;
    }

    public boolean isEmpty() {
        return getSize() == 0;
    }

    public int getSize() {
        return cache.length;
    }

    public void clearEdits() {
        edits.clear();
    }

    public void addEdit(final Edit edit) {
        editIndex++;
        while (editIndex <= edits.size()) {
            edits.remove(edits.size() - 1);
        }
        edits.add(edit);
    }

    public int getSelectedAddress() {
        final int start = startSelectedAddress;
        final int end = endSelectedAddress;
        if (start < 0 || end < 0) {
            return 0;
        }
        final int min = start < end ? start : end;
        if (min >= cache.length) {
            return cache.length - 1;
        }
        return min;
    }

    public int getStartSelectedAddress() {
        return startSelectedAddress;
    }

    public void setStartSelectedAddress(int startSelectedAddress) {
        if (startSelectedAddress < 0) {
            startSelectedAddress = 0;
        }
        this.startSelectedAddress = startSelectedAddress;
    }

    public void setSelection(final int address) {
        setSelection(address, address);
    }

    public void setSelection(final int startAddress, final int endAddress) {
        setStartSelectedAddress(startAddress);
        setEndSelectedAddress(endAddress);
    }

    public int getEndSelectedAddress() {
        return endSelectedAddress;
    }

    public void setEndSelectedAddress(int endSelectedAddress) {
        if (endSelectedAddress < 0) {
            endSelectedAddress = 0;
        }
        this.endSelectedAddress = endSelectedAddress;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(final int scrollY) {
        this.scrollY = scrollY;
    }

    public List<Edit> getEdits() {
        return edits;
    }

    public int getEditIndex() {
        return editIndex;
    }

    public void setEditIndex(final int editIndex) {
        this.editIndex = editIndex;
    }
}
