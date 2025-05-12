package cn.kinlon.emu.input.dipswitches;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static cn.kinlon.emu.utils.CollectionsUtil.isBlank;

public class DipSwitch {

    private final List<DipSwitchValue> values = new ArrayList<>();
    private final String name;
    private final int defaultValue;

    public DipSwitch(final String name, final int defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
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

    @Override
    public String toString() {
        return "DipSwitch{" + "values=" + values + ", name=" + name
                + ", defaultValue=" + defaultValue + '}';
    }
}
