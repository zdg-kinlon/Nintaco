package cn.kinlon.emu.gui.hexeditor;

public class DataSourceCharSequence implements CharSequence {

    private final CharTable charTable;
    private final DataSource source;
    private final int startIndex;
    private final int length;

    public DataSourceCharSequence(final DataSource source,
                                  final CharTable charTable) {
        this(source, charTable, 0, source.getSize());
    }

    public DataSourceCharSequence(final DataSource source,
                                  final CharTable charTable, final int startIndex, final int endIndex) {
        this.source = source;
        this.charTable = charTable;
        this.startIndex = startIndex;
        this.length = endIndex - startIndex;
    }

    public DataSource getSource() {
        return source;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return charTable.getChar(source.peek(startIndex + index));
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new DataSourceCharSequence(source, charTable, start, end);
    }
}
