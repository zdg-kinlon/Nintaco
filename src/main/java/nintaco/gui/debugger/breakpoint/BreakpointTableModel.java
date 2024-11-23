package nintaco.gui.debugger.breakpoint;

import nintaco.Breakpoint;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static nintaco.gui.debugger.breakpoint.BreakpointColumns.*;
import static nintaco.util.StringUtil.parseInt;

public class BreakpointTableModel extends AbstractTableModel {

    public static final String[] TYPE_NAMES
            = {"Execute", "Read", "Write", "Access"};
    private static final String[] COLUMN_NAMES
            = {"Enabled", "Type", "Bank", "Start", "End"};
    private List<Breakpoint> rows = new ArrayList<>();

    public static int getType(final String name) {
        switch (name) {
            case "Execute":
                return 0;
            case "Read":
                return 1;
            case "Write":
                return 2;
            case "Access":
                return 3;
            default:
                return -1;
        }
    }

    public int add(final Breakpoint breakpoint) {
        final int index = rows.indexOf(breakpoint);
        if (index >= 0) {
            rows.set(index, breakpoint);
            fireTableRowsUpdated(index, index);
            return index;
        }
        rows.add(breakpoint);
        fireTableDataChanged();
        return rows.indexOf(breakpoint);
    }

    public void delete(final int index) {
        rows.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }

    public List<Breakpoint> getRowsCopy() {
        final List<Breakpoint> rs = new ArrayList<>();
        for (final Breakpoint row : rows) {
            rs.add(new Breakpoint(row));
        }
        return rs;
    }

    public void setRowsCopy(final List<Breakpoint> rows) {
        this.rows.clear();
        for (final Breakpoint row : rows) {
            this.rows.add(new Breakpoint(row));
        }
        fireTableDataChanged();
    }

    public List<Breakpoint> getRows() {
        return rows;
    }

    public void setRows(List<Breakpoint> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public Breakpoint getRow(final int rowIndex) {
        return rows.get(rowIndex);
    }

    public Breakpoint getRowCopy(final int rowIndex) {
        return new Breakpoint(getRow(rowIndex));
    }

    public void setRow(final int rowIndex, final Breakpoint breakpoint) {
        rows.set(rowIndex, breakpoint);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void setRowCopy(final int rowIndex, final Breakpoint breakpoint) {
        setRow(rowIndex, new Breakpoint(breakpoint));
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
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final Breakpoint row = rows.get(rowIndex);
        switch (columnIndex) {
            case ENABLED:
                return row.isEnabled();
            case TYPE:
                return TYPE_NAMES[row.getType()];
            case BANK:
                return row.getBank() >= 0 ? String.format("$%02X", row.getBank()) : "";
            case START_ADDRESS:
                return String.format("$%04X", row.getStartAddress());
            case END_ADDRESS:
                return row.getEndAddress() >= 0
                        ? String.format("$%04X", row.getEndAddress()) : "";
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex,
                           final int columnIndex) {
        final Breakpoint row = rows.get(rowIndex);
        switch (columnIndex) {
            case ENABLED:
                row.setEnabled((boolean) value);
                break;
            case TYPE:
                switch ((String) value) {
                    case "Execute":
                        row.setType(0);
                        break;
                    case "Read":
                        row.setType(1);
                        break;
                    case "Write":
                        row.setType(2);
                        break;
                    case "Access":
                        row.setType(3);
                        break;
                }
                break;
            case BANK: {
                final int bank = parseInt(value, true, 0xFF);
                row.setBank(bank >= 0 ? bank : -1);
                break;
            }
            case START_ADDRESS: {
                final int startAddress = parseInt(value, true, 0xFFFF);
                if (startAddress >= 0 && (!row.isRange()
                        || startAddress <= row.getEndAddress())) {
                    row.setStartAddress(startAddress);
                }
                break;
            }
            case END_ADDRESS: {
                final int endAddress = parseInt(value, true, 0xFFFF);
                if (endAddress < 0) {
                    row.setEndAddress(-1);
                } else if (endAddress >= row.getStartAddress()) {
                    row.setEndAddress(endAddress);
                }
                break;
            }
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return true;
    }
}
