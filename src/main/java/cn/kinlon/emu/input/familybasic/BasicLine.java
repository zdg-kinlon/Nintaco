package cn.kinlon.emu.input.familybasic;

public class BasicLine implements Comparable<BasicLine> {

    private final int lineNumber;
    private final int[] data;

    public BasicLine(final int lineNumber, final int[] data) {
        this.lineNumber = lineNumber;
        this.data = data;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return lineNumber;
    }

    @Override
    public boolean equals(final Object obj) {
        return lineNumber == ((BasicLine) obj).lineNumber;
    }

    @Override
    public int compareTo(final BasicLine line) {
        return Integer.compare(lineNumber, line.lineNumber);
    }
}