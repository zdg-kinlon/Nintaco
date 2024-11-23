package nintaco.disassembler;

public class AddressTextRange {

    protected int bank;
    protected int address;
    protected int start;
    protected int end;

    public AddressTextRange() {
    }

    public AddressTextRange(final int bank, final int address, final int start,
                            final int end) {
        this.bank = bank;
        this.address = address;
        this.start = start;
        this.end = end;
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
