package cn.kinlon.emu.input;

import java.util.Objects;

public class ControllerButton {

    public ControllerQueue controllerQueue;
    public int value;
    public int hash;
    public String name;

    public ControllerButton() {
    }

    public ControllerButton(final ControllerQueue controllerQueue,
                            final int value, final String name) {
        setAll(controllerQueue, value, name);
    }

    public final ControllerButton setAll(final ControllerQueue controllerQueue,
                                         final int value, final String name) {
        this.controllerQueue = controllerQueue;
        this.value = value;
        this.name = name;
        updateHash();
        return this;
    }

    private void updateHash() {
        hash = controllerQueue.index ^ value ^ Objects.hashCode(name);
    }

    @Override
    public boolean equals(final Object obj) {
        final ControllerButton c = (ControllerButton) obj;
        return value == c.value && controllerQueue.index == c.controllerQueue.index
                && Objects.equals(name, c.name);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
