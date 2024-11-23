package nintaco.gui.ramwatch;

import java.io.Serializable;

import static nintaco.gui.ramsearch.RamSearchTableRow.HEX_FORMATS;
import static nintaco.gui.ramsearch.RamSearchTableRow.formatValue;

public class RamWatchRow implements Serializable {

    private static final long serialVersionUID = 0;

    private int address;
    private int wordSizeIndex;
    private int valueFormat;
    private String description = " ";
    private boolean separator;

    private transient int value;
    private transient String addressStr;
    private transient String valueStr;

    public RamWatchRow() {
    }

    public RamWatchRow(final RamWatchRow row) {
        set(row);
    }

    public final void set(final RamWatchRow row) {
        this.address = row.address;
        this.value = row.value;
        this.description = row.description;
        this.separator = row.separator;
        this.addressStr = row.addressStr;
        this.valueStr = row.valueStr;
        this.wordSizeIndex = row.wordSizeIndex;
        this.valueFormat = row.valueFormat;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
        addressStr = null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        valueStr = null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddressStr() {
        if (addressStr == null) {
            addressStr = String.format("%04X", address);
        }
        return addressStr;
    }

    public String getValueStr() {
        if (valueStr == null) {
            valueStr = formatValue(value, valueFormat, wordSizeIndex,
                    HEX_FORMATS[wordSizeIndex]);
        }
        return valueStr;
    }

    public int getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(final int valueFormat) {
        this.valueFormat = valueFormat;
        valueStr = null;
    }

    public int getWordSizeIndex() {
        return wordSizeIndex;
    }

    public final void setWordSizeIndex(final int wordSizeIndex) {
        this.wordSizeIndex = wordSizeIndex;
        valueStr = null;
    }
}
