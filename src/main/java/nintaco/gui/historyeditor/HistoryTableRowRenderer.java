package nintaco.gui.historyeditor;

import nintaco.gui.IntPoint;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HistoryTableRowRenderer extends DefaultTableCellRenderer {

    private static final Color LIGHT_BLUE = new Color(0xDDFFFF);
    private static final Color BLUE = new Color(0xC3F4F4);
    private static final Color DARK_BLUE = new Color(0xACE5E5);

    private static final Color GRAY = new Color(0xEDEDED);
    private static final Color DARK_GRAY = new Color(0xE2E2E2);

    private final IntPoint point = new IntPoint();

    private Font monospacedFont;

    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value, final boolean isSelected, final boolean hasFocus,
                                                   final int rowIndex, final int columnIndex) {

        super.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, rowIndex, columnIndex);

        if (columnIndex < 2) {
            final Font tableFont = table.getFont();
            if (monospacedFont == null
                    || monospacedFont.getSize() != tableFont.getSize()) {
                monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN,
                        tableFont.getSize());
            }
            setFont(monospacedFont);
            setHorizontalAlignment(columnIndex == 0 ? RIGHT : CENTER);
        } else {
            setFont(table.getFont());
            setHorizontalAlignment(CENTER);
        }
        setBorder(noFocusBorder);

        final HistoryTableModel historyTableModel
                = (HistoryTableModel) table.getModel();
        if (columnIndex >= 2) {
            point.set(columnIndex, rowIndex);
            final Color foreground = historyTableModel.getHotCells().get(point);
            if (foreground == null) {
                setForeground(Color.BLACK);
            } else {
                if (!isSelected) {
                    setForeground(foreground);
                }
                final String s = (String) value;
                if (s != null && s.isEmpty()) {
                    setText("-");
                }
            }
        } else if (!isSelected) {
            setForeground(Color.BLACK);
        }

        if (!isSelected) {
            final int headIndex = historyTableModel.getHeadIndex();
            final int tailIndex = historyTableModel.getTailIndex();
            final boolean headRow = rowIndex == headIndex;
            if (columnIndex == 0) {
                setBackground(Color.WHITE);
            } else if (columnIndex == 1) {
                setBackground(headRow || rowIndex > tailIndex ? Color.WHITE
                        : LIGHT_BLUE);
            } else {
                final boolean colorTest = (((columnIndex - 2) >> 3) & 1) == 0;
                if (rowIndex > tailIndex) {
                    setBackground(colorTest ? GRAY : DARK_GRAY);
                } else if (headRow) {
                    setBackground(colorTest ? LIGHT_BLUE : BLUE);
                } else {
                    setBackground(colorTest ? BLUE : DARK_BLUE);
                }
            }
        }

        return this;
    }
}
