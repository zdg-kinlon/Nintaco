package nintaco.gui.ramsearch;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RamSearchTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES
            = {"Address", "Current", "Prior", "Changes"};

    private List<RamSearchTableRow> rows = new ArrayList<>();

    public RamSearchTableModel() {
    }

    public RamSearchTableModel(final List<RamSearchTableRow> rows) {
        this.rows = rows;
    }

    public RamSearchTableRow getRow(final int rowIndex) {
        return rows.get(rowIndex);
    }

    public List<RamSearchTableRow> getRows() {
        return rows;
    }

    public void setRows(List<RamSearchTableRow> rows) {
        this.rows = rows;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final RamSearchTableRow row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.getAddressStr();
            case 1:
                return row.getCurrentStr();
            case 2:
                return row.getPriorStr();
            case 3:
                return row.getChangesStr();
            default:
                return null;
        }
    }
}
