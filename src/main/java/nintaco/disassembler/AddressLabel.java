package nintaco.disassembler;

import java.io.Serializable;

public class AddressLabel implements Comparable<AddressLabel>, Serializable {

    private static final long serialVersionUID = 0;
    private int bank;
    private int address;
    private String label;
    private String comment;
    private boolean code;
    private boolean bookmark;
    public AddressLabel(final AddressLabel addressLabel) {
        this(addressLabel.bank, addressLabel.address, addressLabel.label,
                addressLabel.comment, addressLabel.code, addressLabel.bookmark);
    }

    public AddressLabel(final int bank, final int address, final String label,
                        final String comment, final boolean code, final boolean bookmark) {
        setBank(bank);
        this.address = address;
        this.label = label;
        this.comment = comment;
        this.code = code;
        this.bookmark = bookmark;
    }

    public static int createKey(final int bank, final int address) {
        return (bank << 16) | address;
    }

    public int getKey() {
        return createKey(bank, address);
    }

    public int getBank() {
        return bank;
    }

    public final void setBank(int bank) {
        this.bank = bank >= 0 ? bank : -1;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(final int address) {
        this.address = address;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public boolean isCode() {
        return code;
    }

    public void setCode(boolean code) {
        this.code = code;
    }

    public boolean isBookmark() {
        return bookmark;
    }

    public void setBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public int compareTo(final AddressLabel addressLabel) {
        return Integer.compare(getKey(), addressLabel.getKey());
    }

    @Override
    public boolean equals(Object obj) {
        final AddressLabel addressLabel = (AddressLabel) obj;
        return getKey() == addressLabel.getKey();
    }

    @Override
    public int hashCode() {
        return getKey();
    }

    @Override
    public String toString() {
        return label;
    }
}
