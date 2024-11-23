package nintaco.gui;

public class Int {

    public int value;

    public Int() {
    }

    public Int(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public boolean equals(final Object obj) {
        return ((Int) obj).value == value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
