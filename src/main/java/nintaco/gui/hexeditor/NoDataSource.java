package nintaco.gui.hexeditor;

public class NoDataSource extends DataSource {

    private final int index;

    public NoDataSource(int index) {
        super(0);
        this.index = index;
    }

    @Override
    public int peek(int address) {
        return 0;
    }

    @Override
    public void write(int address, int value) {
    }

    @Override
    public int getIndex() {
        return index;
    }
}
