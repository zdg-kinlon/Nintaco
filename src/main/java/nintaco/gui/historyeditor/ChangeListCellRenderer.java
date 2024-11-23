package nintaco.gui.historyeditor;

import javax.swing.*;
import java.awt.*;

public class ChangeListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
        final ChangeListModel model = (ChangeListModel) list.getModel();
        final Component component = super.getListCellRendererComponent(list, value,
                index, model.getChangesIndex() - 1 == index, false);
        component.setForeground(index >= model.getChangesIndex()
                ? Color.LIGHT_GRAY : Color.BLACK);
        return component;
    }
}
