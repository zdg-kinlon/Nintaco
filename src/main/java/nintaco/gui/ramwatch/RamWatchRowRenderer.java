package nintaco.gui.ramwatch;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RamWatchRowRenderer extends DefaultTableCellRenderer {

    private static final Border leftBorder = new EmptyBorder(0, 8, 0, 0);
    private static final Separator separator = new Separator();

    private final Border border;
    private Font monospacedFont;

    public RamWatchRowRenderer(final boolean rightJustified) {
        setHorizontalAlignment(rightJustified ? SwingConstants.RIGHT
                : SwingConstants.LEFT);
        border = rightJustified ? noFocusBorder : leftBorder;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   Object value, final boolean isSelected, final boolean hasFocus,
                                                   final int rowIndex, final int columnIndex) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                rowIndex, columnIndex);

        final RamWatchTableModel tableModel = (RamWatchTableModel) table.getModel();
        if (rowIndex >= 0 && rowIndex < tableModel.getRowCount()) {
            final RamWatchRow row = tableModel.getRow(rowIndex);
            if (row.isSeparator()) {
                separator.setBackground(getBackground());
                separator.setForeground(getForeground());
                return separator;
            }
        }

        final Font tableFont = table.getFont();
        if (monospacedFont == null
                || monospacedFont.getSize() != tableFont.getSize()) {
            monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN,
                    tableFont.getSize());
        }
        setFont(monospacedFont);
        setBorder(border);
        return this;
    }
}
