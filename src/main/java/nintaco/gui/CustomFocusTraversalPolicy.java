package nintaco.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomFocusTraversalPolicy extends FocusTraversalPolicy {

    private final List<Component> order = new ArrayList<>();

    public void add(Component component) {
        order.add(component);
    }

    private int getIndex(Component component) {
        int index = -1;
        while (component != null && index < 0) {
            index = order.indexOf(component);
            component = component.getParent();
        }
        return index;
    }

    @Override
    public Component getComponentAfter(Container container, Component component) {
        int index = getIndex(component) + 1;
        if (index >= order.size()) {
            index = 0;
        }
        Component after = order.get(index);
        while (index < order.size() && !(after.isEnabled() && after.isVisible())) {
            index++;
            after = order.get(index);
        }
        return after;
    }

    @Override
    public Component getComponentBefore(Container container, Component component) {
        int index = getIndex(component) - 1;
        if (index < 0) {
            index = order.size() - 1;
        }
        Component before = order.get(index);
        while (index >= 0 && !(before.isEnabled() && before.isVisible())) {
            index--;
            before = order.get(index);
        }
        return before;
    }

    @Override
    public Component getFirstComponent(Container container) {
        int index = 0;
        Component first = order.get(index);
        while (index < order.size() && !(first.isEnabled() && first.isVisible())) {
            index++;
            first = order.get(index);
        }
        return first;
    }

    @Override
    public Component getLastComponent(Container container) {
        int index = order.size() - 1;
        Component last = order.get(index);
        while (index >= 0 && !(last.isEnabled() && last.isVisible())) {
            index--;
            last = order.get(index);
        }
        return last;
    }

    @Override
    public Component getDefaultComponent(Container container) {
        return getFirstComponent(container);
    }
}