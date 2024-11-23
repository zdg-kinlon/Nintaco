package nintaco.gui.cheats.search;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class RamTableModel extends AbstractTableModel {

    static final String[] COLUMN_NAMES = {"Address", "R0", "R1"};
    private List<RamTableRow> rows;
    private int base;
    public RamTableModel(List<RamTableRow> rows) {
        this.rows = rows;
    }

    public RamTableRow getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public List<RamTableRow> getRows() {
        return rows;
    }

    public void setRows(List<RamTableRow> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public void setShowHex(boolean showHex) {
        base = showHex ? RamTableRow.HEXIDECIMAL : RamTableRow.DECIMAL;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).getValueAt(columnIndex, base);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    interface Indices {
        int ADDRESS = 0;
        int R0 = 1;
        int R1 = 2;
    }
}
