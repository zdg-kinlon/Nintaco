package nintaco.disassembler;

public class Instruction extends AddressTextRange {

    private final AddressTextRange[] ranges = new AddressTextRange[4];

    private int length;
    private String mnemonic;
    private String description;
    private String machineCode;
    private int descriptionLines = 1;
    private int line;

    public Instruction() {
    }

    public Instruction(final int address, final int length,
                       final String description) {
        this.address = address;
        this.length = length;
        this.description = description;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(final String machineCode) {
        this.machineCode = machineCode;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(final String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getDescriptionLines() {
        return descriptionLines;
    }

    public void setDescriptionLines(final int descriptionLines) {
        this.descriptionLines = descriptionLines;
    }

    public AddressTextRange[] getRanges() {
        return ranges;
    }

    public int getLine() {
        return line;
    }

    public void setLine(final int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return description;
    }
}
