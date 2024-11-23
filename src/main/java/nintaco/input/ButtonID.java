package nintaco.input;

import java.io.Serializable;
import java.util.Objects;

import static nintaco.util.CollectionsUtil.compare;
import static nintaco.util.StringUtil.isBlank;

public class ButtonID implements Serializable, Comparable<ButtonID> {

    private static final long serialVersionUID = 0;

    private final InputDeviceID device;
    private final String name;
    private final int value;
    private final String description;

    public ButtonID() {
        this(null, null, 0);
    }

    public ButtonID(final InputDeviceID device, final String name,
                    final int value) {

        this.device = device;
        this.name = name;
        this.value = value;

        final StringBuilder sb = new StringBuilder();
        if (device == null) {
            sb.append("...");
        } else {
            if (!isBlank(device.getDescription())) {
                sb.append('(').append(device.getDescription()).append(") ");
            }
            if (value < 0) {
                sb.append('-');
            }
            if (name != null) {
                sb.append(" ".equals(name) ? "Space" : name);
            }
        }
        description = sb.toString();
    }

    public InputDeviceID getDevice() {
        return device;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int hashCode() {
        return value ^ Objects.hashCode(name) ^ Objects.hashCode(device);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final ButtonID b = (ButtonID) obj;
        return value == b.value && Objects.equals(name, b.name)
                && Objects.equals(device, b.device);
    }

    @Override
    public int compareTo(final ButtonID b) {
        if (b == null) {
            return 1;
        }
        if (this == b) {
            return 0;
        }
        int v = Integer.compare(value, b.value);
        if (v == 0) {
            v = compare(name, b.name);
            if (value == 0) {
                return compare(device, b.device);
            } else {
                return v;
            }
        } else {
            return v;
        }
    }
}
