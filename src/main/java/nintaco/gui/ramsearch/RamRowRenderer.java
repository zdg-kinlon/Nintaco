package nintaco.gui.ramsearch;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RamRowRenderer extends DefaultTableCellRenderer {

    private Font monospacedFont;

    public RamRowRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value, final boolean isSelected, final boolean hasFocus,
                                                   final int rowIndex, final int columnIndex) {

        final Component component = super.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, rowIndex, columnIndex);
        final RamSearchTableRow row = ((RamSearchTableModel) table.getModel())
                .getRow(rowIndex);
        component.setForeground(row.isFlagged() ? Color.RED : Color.BLACK);
        final Font tableFont = table.getFont();
        if (monospacedFont == null
                || monospacedFont.getSize() != tableFont.getSize()) {
            monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN,
                    tableFont.getSize());
        }
        component.setFont(monospacedFont);
        setBorder(noFocusBorder);
        return component;
    }
}
