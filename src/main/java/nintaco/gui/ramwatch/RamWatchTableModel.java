package nintaco.gui.ramwatch;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RamWatchTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES
            = {"Address", "Value", "Description"};

    private List<RamWatchRow> rows = new ArrayList<>();

    public RamWatchTableModel() {
    }

    public RamWatchTableModel(final List<RamWatchRow> rows) {
        this.rows = rows;
    }

    public List<RamWatchRow> getRows() {
        return rows;
    }

    public void setRows(final List<RamWatchRow> rows) {
        this.rows = rows;
    }

    public void copyRows(final List<RamWatchRow> rows) {
        this.rows.clear();
        for (final RamWatchRow row : rows) {
            this.rows.add(new RamWatchRow(row));
        }
    }

    public RamWatchRow getRow(final int rowIndex) {
        return rows.get(rowIndex);
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
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final RamWatchRow row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.getAddressStr();
            case 1:
                return row.getValueStr();
            default:
                return row.getDescription();
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }
}
