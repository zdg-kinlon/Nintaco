package nintaco.cheats;

import java.io.Serializable;
import java.util.Objects;

import static nintaco.util.StringUtil.isBlank;

public final class Cheat implements Serializable {

    private static final long serialVersionUID = 0;

    private int address;
    private int dataValue;
    private int compareValue;
    private boolean hasCompareValue;

    private String description = "";
    private boolean enabled = true;

    public Cheat() {
    }

    public Cheat(final int address, final int dataValue) {
        this(address, dataValue, -1);
    }

    public Cheat(final int address, final int dataValue, final int compareValue) {
        setAddress(address);
        setDataValue(dataValue);
        setCompareValue(compareValue);
    }

    public Cheat(final Cheat cheat) {
        this.address = cheat.address;
        this.dataValue = cheat.dataValue;
        this.compareValue = cheat.compareValue;
        this.hasCompareValue = cheat.hasCompareValue;
        this.description = cheat.description;
        this.enabled = cheat.enabled;
    }

    @Override
    public boolean equals(final Object obj) {
        final Cheat cheat = (Cheat) obj;
        return effectivelyEquals(cheat)
                && enabled == cheat.enabled
                && Objects.equals(description, cheat.description);
    }

    public boolean effectivelyEquals(final Cheat cheat) {
        return address == cheat.address
                && dataValue == cheat.dataValue
                && hasCompareValue == cheat.hasCompareValue
                && (!hasCompareValue || compareValue == cheat.compareValue);
    }

    public void generateDescription() {
        if (hasCompareValue) {
            description = String.format("$%04X : %d > %d", address, compareValue,
                    dataValue);
        } else {
            description = String.format("$%04X : %d", address, dataValue);
        }
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(final int address) {
        this.address = address & 0xFFFF;
    }

    public int getDataValue() {
        return dataValue;
    }

    public void setDataValue(final int dataValue) {
        this.dataValue = dataValue & 0xFF;
    }

    public int getCompareValue() {
        return compareValue;
    }

    public void setCompareValue(final int compareValue) {
        this.compareValue = compareValue & 0xFF;
        this.hasCompareValue = compareValue >= 0;
    }

    public boolean hasCompareValue() {
        return hasCompareValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int apply(final int address, final int value) {
        return this.address == address
                && (!hasCompareValue || compareValue == value) ? dataValue : -1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("Cheat { address = %04X, data = %02X, ", address,
                dataValue));
        if (hasCompareValue) {
            sb.append(String.format("compareValue = %02X, ", compareValue));
        }
        sb.append(String.format("hasCompareValue = %b, enabled = %b",
                hasCompareValue, enabled));
        if (!isBlank(description)) {
            sb.append(", description = ").append(description);
        }
        sb.append(" }");
        return sb.toString();
    }
}
