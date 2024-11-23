package nintaco.gui.ramsearch;

import static nintaco.gui.ramsearch.ValueFormat.*;

public class RamSearchTableRow extends RamRow {

    public static final String[] HEX_FORMATS = {"%02X", "%04X", "%08X"};

    protected int address;

    private String addressStr;
    private String currentStr;
    private String priorStr;
    private String changesStr;

    private int wordSizeIndex;
    private int valueFormat;
    private String hexFormat;

    public RamSearchTableRow() {
    }

    public RamSearchTableRow(final RamRow row) {
        setChanges(row.changes);
        setCurrent(row.current);
        setFlagged(row.flagged);
        setPrior(row.prior);
    }

    public RamSearchTableRow(final RamSearchTableRow row) {
        address = row.address;
        addressStr = row.addressStr;
        current = row.current;
        currentStr = row.currentStr;
        prior = row.prior;
        priorStr = row.priorStr;
        changes = row.changes;
        changesStr = row.changesStr;
        flagged = row.flagged;
        valueFormat = row.valueFormat;
        hexFormat = row.hexFormat;
    }

    public RamSearchTableRow(final int address, final int current,
                             final int prior, final int changes, final int valueFormat,
                             final int wordSizeIndex) {
        this.address = address;
        this.current = current;
        this.prior = prior;
        this.changes = changes;
        this.valueFormat = valueFormat;
        setWordSizeIndex(wordSizeIndex);
    }

    public static String formatValue(final int value, final int valueFormat,
                                     final int wordSizeIndex, final String hexFormat) {
        switch (valueFormat) {
            case Signed:
                switch (wordSizeIndex) {
                    case 0:
                        return Byte.toString((byte) value);
                    case 1:
                        return Short.toString((short) value);
                    case 2:
                        return Integer.toString(value);
                }
            case Unsigned:
                return Integer.toUnsignedString(value);
            case Hex:
                return String.format(hexFormat, value);
            default:
                return null;
        }
    }

    public int getWordSizeIndex() {
        return wordSizeIndex;
    }

    public final void setWordSizeIndex(final int wordSizeIndex) {
        this.wordSizeIndex = wordSizeIndex;
        this.hexFormat = HEX_FORMATS[wordSizeIndex];
        currentStr = null;
        priorStr = null;
    }

    public int getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(final int valueFormat) {
        this.valueFormat = valueFormat;
        currentStr = null;
        priorStr = null;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(final int address) {
        this.address = address;
        addressStr = null;
    }

    @Override
    public int getCurrent() {
        return current;
    }

    @Override
    public final void setCurrent(final int current) {
        this.current = current;
        currentStr = null;
    }

    @Override
    public int getPrior() {
        return prior;
    }

    @Override
    public final void setPrior(final int prior) {
        this.prior = prior;
        priorStr = null;
    }

    @Override
    public int getChanges() {
        return changes;
    }

    @Override
    public final void setChanges(final int changes) {
        this.changes = changes;
        changesStr = null;
    }

    public String getAddressStr() {
        if (addressStr == null) {
            addressStr = String.format("%04X", address);
        }
        return addressStr;
    }

    public String getCurrentStr() {
        if (currentStr == null) {
            currentStr = formatValue(current, valueFormat, wordSizeIndex, hexFormat);
        }
        return currentStr;
    }

    public String getPriorStr() {
        if (priorStr == null) {
            priorStr = formatValue(prior, valueFormat, wordSizeIndex, hexFormat);
        }
        return priorStr;
    }

    public String getChangesStr() {
        if (changesStr == null) {
            changesStr = Integer.toString(changes);
        }
        return changesStr;
    }
}
