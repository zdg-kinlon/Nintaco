package nintaco.input;

import java.io.Serializable;
import java.util.Objects;

import static nintaco.util.CollectionsUtil.compare;

public class InputDeviceID implements Serializable, Comparable<InputDeviceID> {

    private static final long serialVersionUID = 0;

    private final int index;
    private final String id;
    private final String description;

    public InputDeviceID() {
        this(0, null, null);
    }

    public InputDeviceID(final int index, final String id,
                         final String description) {

        this.index = index;
        this.id = id;
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        }
        final InputDeviceID d = (InputDeviceID) obj;
        return index == d.index && Objects.equals(id, d.id);
    }

    @Override
    public int hashCode() {
        return index ^ Objects.hashCode(id);
    }

    @Override
    public int compareTo(final InputDeviceID d) {
        if (d == null) {
            return 1;
        }
        if (this == d) {
            return 0;
        }
        final int value = Integer.compare(index, d.index);
        if (value == 0) {
            return compare(id, d.id);
        } else {
            return value;
        }
    }
}
