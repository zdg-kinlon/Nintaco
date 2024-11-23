package nintaco;

import java.io.*;

public class Breakpoint implements Serializable {

    private static final long serialVersionUID = 0;

    public int type;
    public int bank = -1;
    public int startAddress;
    public int endAddress = -1;
    public boolean enabled = true;
    public boolean range;

    public transient boolean hit;

    public Breakpoint() {
    }

    public Breakpoint(final Breakpoint breakpoint) {
        this(breakpoint.type, breakpoint.bank, breakpoint.startAddress,
                breakpoint.endAddress, breakpoint.enabled);
    }

    public Breakpoint(final int type, final int bank, final int startAddress,
                      final int endAddress, final boolean enabled) {
        setType(type);
        setBank(bank);
        setStartAddress(startAddress);
        setEndAddress(endAddress);
        setEnabled(enabled);
    }

    public boolean isRange() {
        return range;
    }

    public int getType() {
        return type;
    }

    public final void setType(int type) {
        this.type = type;
    }

    public int getBank() {
        return bank;
    }

    public final void setBank(int bank) {
        this.bank = bank >= 0 ? bank : -1;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public final void setStartAddress(int startAddress) {
        this.startAddress = startAddress >= 0 ? startAddress : -1;
    }

    public int getEndAddress() {
        return endAddress;
    }

    public final void setEndAddress(int endAddress) {
        range = endAddress >= 0;
        this.endAddress = range ? endAddress : -1;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHit() {
        return hit;
    }

    public final void setHit(boolean hit) {
        this.hit = hit;
    }

    @Override
    public int hashCode() {
        return type ^ bank ^ startAddress ^ endAddress;
    }

    @Override
    public boolean equals(final Object obj) {
        final Breakpoint breakpoint = (Breakpoint) obj;
        return breakpoint.type == type && breakpoint.bank == bank
                && breakpoint.startAddress == startAddress
                && breakpoint.endAddress == endAddress;
    }
}
