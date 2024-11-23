package nintaco.gui;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class LocalIPAddressRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {

        return super.getListCellRendererComponent(list,
                value == null ? "localhost" : ((InetAddress) value).getHostAddress(),
                index, isSelected, cellHasFocus);
    }
}
