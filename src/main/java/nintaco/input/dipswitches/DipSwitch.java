package nintaco.input.dipswitches;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static nintaco.util.CollectionsUtil.isBlank;

public class DipSwitch {

    private final List<DipSwitchValue> values = new ArrayList<>();
    private final String name;
    private final int defaultValue;

    public DipSwitch(final String name, final int defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public static int evaluate(final List<DipSwitch> dipSwitches,
                               final int[] dipSwitchValues) {

        if (isBlank(dipSwitches) || dipSwitchValues == null) {
            return 0;
        }

        int value = 0;
        for (int i = min(dipSwitches.size(), dipSwitchValues.length) - 1; i >= 0;
             i--) {
            final List<DipSwitchValue> values = dipSwitches.get(i).getValues();
            if (dipSwitchValues[i] < values.size()) {
                value |= values.get(dipSwitchValues[i]).getValue();
            }
        }
        return value;
    }

    public static List<DipSwitch> createDefaultDipSwitches() {

        final List<DipSwitch> dipSwitches = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            dipSwitches.add(new DipSwitch("Unknown", 0)
                    .add("Off", 0x00)
                    .add("On", 1 << i));
        }

        return dipSwitches;
    }

    public DipSwitch add(final String name, final int value) {
        return add(new DipSwitchValue(name, value));
    }

    public DipSwitch add(final DipSwitchValue value) {
        values.add(value);
        return this;
    }

    public List<DipSwitchValue> getValues() {
        return values;
    }

    public String getName() {
        return name;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "DipSwitch{" + "values=" + values + ", name=" + name
                + ", defaultValue=" + defaultValue + '}';
    }
}
