package nintaco.gui.input.buttonmapping;

import nintaco.input.ButtonMapping;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ButtonMappingTableModel extends AbstractTableModel {

    private final List<ButtonMapping> rows;

    public ButtonMappingTableModel(final List<ButtonMapping> rows) {
        this.rows = rows;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(final int column) {
        return column == 0 ? "Button" : "Mapping";
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final ButtonMapping mapping = rows.get(rowIndex);
        return columnIndex == 0 ? mapping.getButtonName()
                : mapping.getDescription();
    }
}
