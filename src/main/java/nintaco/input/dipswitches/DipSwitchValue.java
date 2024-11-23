package nintaco.input.dipswitches;

public class DipSwitchValue {

    private final String name;
    private final int value;

    public DipSwitchValue(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DipSwitchValue{" + "name=" + name + ", value=" + value + '}';
    }
}
