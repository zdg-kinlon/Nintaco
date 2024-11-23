package nintaco.gui.ramsearch;

public class MemoryRange {

    public static final int RAM = 0;
    public static final int RAM_NVRAM = 1;
    public static final int ROM = 2;

    public final int startAddress;
    public final int endAddress;

    public MemoryRange(final int startAddress, final int endAddress) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getEndAddress() {
        return endAddress;
    }
}
