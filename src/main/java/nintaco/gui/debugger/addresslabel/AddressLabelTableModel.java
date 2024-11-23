package nintaco.gui.debugger.addresslabel;

import nintaco.disassembler.AddressLabel;
import nintaco.preferences.GamePrefs;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static nintaco.gui.debugger.addresslabel.AddressLabelColumns.*;
import static nintaco.util.StringUtil.ParseErrors;
import static nintaco.util.StringUtil.parseInt;

public class AddressLabelTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES
            = {"Code", "Bookmark", "Bank", "Address", "Label", "Comment"};

    private List<AddressLabel> rows = new ArrayList<>();

    public List<AddressLabel> getRows() {
        return rows;
    }

    public void setRows(final List<AddressLabel> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public void delete(final int index) {
        rows.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public int add(final AddressLabel addressLabel) {
        final int index = rows.indexOf(addressLabel);
        if (index >= 0) {
            rows.set(index, addressLabel);
            fireTableRowsUpdated(index, index);
            return index;
        }
        rows.add(addressLabel);
        fireTableDataChanged();
        return rows.indexOf(addressLabel);
    }

    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }

    public List<AddressLabel> getRowsCopy() {
        final List<AddressLabel> rs = new ArrayList<>();
        for (final AddressLabel row : rows) {
            rs.add(new AddressLabel(row));
        }
        return rs;
    }

    public void setRowsCopy(final List<AddressLabel> rows) {
        final List<AddressLabel> rs = new ArrayList<>();
        synchronized (GamePrefs.class) {
            for (final AddressLabel row : rows) {
                rs.add(new AddressLabel(row));
            }
        }
        setRows(rs);
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
        final AddressLabel addressLabel = rows.get(rowIndex);
        switch (columnIndex) {
            case CODE:
                return addressLabel.isCode();
            case BOOKMARK:
                return addressLabel.isBookmark();
            case BANK:
                return addressLabel.getBank() < 0 ? ""
                        : String.format("$%02X", addressLabel.getBank());
            case ADDRESS:
                return String.format("$%04X", addressLabel.getAddress());
            case LABEL:
                return addressLabel.getLabel();
            case COMMENT:
                return addressLabel.getComment();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex,
                           final int columnIndex) {
        if (value == null) {
            return;
        }
        final AddressLabel addressLabel = rows.get(rowIndex);
        switch (columnIndex) {
            case CODE:
                addressLabel.setCode((Boolean) value);
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            case BOOKMARK:
                addressLabel.setBookmark((Boolean) value);
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            case BANK: {
                final int bank = parseInt(value, true, 0xFF);
                if (bank < ParseErrors.EMPTY) {
                    return;
                }
                for (final AddressLabel row : rows) {
                    if (row.getBank() == bank
                            && row.getAddress() == addressLabel.getAddress()) {
                        return;
                    }
                }
                addressLabel.setBank(bank);
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            }
            case ADDRESS: {
                final int address = parseInt(value, true, 0xFFFF);
                if (address < 0) {
                    return;
                }
                for (final AddressLabel row : rows) {
                    if (row.getAddress() == address
                            && row.getBank() == addressLabel.getBank()) {
                        return;
                    }
                }
                addressLabel.setAddress(address);
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            }
            case LABEL: {
                final String label = ((String) value).trim();
                if (!(label.isEmpty() && addressLabel.getComment().isEmpty())) {
                    addressLabel.setLabel(label);
                }
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            }
            case COMMENT: {
                final String comment = ((String) value).trim();
                if (!(comment.isEmpty() && addressLabel.getLabel().isEmpty())) {
                    addressLabel.setComment(comment);
                }
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return (columnIndex < 2) ? Boolean.class : String.class;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }
}
