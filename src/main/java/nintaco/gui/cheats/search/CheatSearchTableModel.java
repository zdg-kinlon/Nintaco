package nintaco.gui.cheats.search;

import nintaco.cheats.Cheat;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static nintaco.gui.cheats.CheatsTableModel.MULTI_PATTERN;

public class CheatSearchTableModel extends AbstractTableModel {

    private final List<Cheat> cheats;

    private boolean modified;

    public CheatSearchTableModel(final List<Cheat> cheats) {
        this.cheats = cheats;
    }

    public void setCheats(final List<Cheat> cheats) {
        this.cheats.clear();
        this.cheats.addAll(cheats);
        modified = false;
        fireTableDataChanged();
    }

    public List<Cheat> getCheatsCopy() {
        final List<Cheat> cs = new ArrayList<>();
        for (final Cheat cheat : cheats) {
            cs.add(new Cheat(cheat));
        }
        return cs;
    }

    public boolean isEmpty() {
        return cheats.isEmpty();
    }

    @Override
    public int getRowCount() {
        return cheats.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    public int add(final Cheat cheat) {
        return CheatSearchTableModel.this.add(cheat, true, false);
    }

    public int add(final Cheat cheat, final boolean fireChangedEvent,
                   final boolean removeDuplicates) {
        if (cheat == null) {
            return 0;
        }
        if (removeDuplicates) {
            for (int i = cheats.size() - 1; i >= 0; i--) {
                final Cheat c = cheats.get(i);
                if (c.effectivelyEquals(cheat)) {
                    cheats.remove(i);
                }
            }
        }
        cheats.add(cheat);
        modified = true;
        if (fireChangedEvent) {
            fireTableDataChanged();
        }
        return cheats.size() - 1;
    }

    public void add(final List<Cheat> cheats, final boolean removeDuplicates) {
        for (final Cheat cheat : cheats) {
            CheatSearchTableModel.this.add(cheat, false, removeDuplicates);
        }
        fireTableDataChanged();
    }

    public void update(final int rowIndex, final Cheat cheat) {
        if (cheat == null || rowIndex < 0 || rowIndex >= cheats.size()) {
            return;
        }
        modified = true;
        final Cheat row = cheats.get(rowIndex);
        cheat.setEnabled(row.isEnabled());
        cheats.set(rowIndex, cheat);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void removeRow(int rowIndex) {
        cheats.remove(rowIndex);
        modified = true;
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public Cheat getRow(int rowIndex) {
        return cheats.get(rowIndex);
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final Cheat cheat = cheats.get(rowIndex);
        if (columnIndex == 0) {
            return cheat.isEnabled();
        } else {
            return cheat.getDescription();
        }
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex,
                           final int columnIndex) {

        final Cheat cheat = cheats.get(rowIndex);
        modified = true;
        if (columnIndex == 0) {
            final Matcher matcher = MULTI_PATTERN.matcher(cheats.get(rowIndex)
                    .getDescription());
            if (matcher.find()) {
                final String prefix = matcher.group(1);
                for (int i = cheats.size() - 1; i >= 0; i--) {
                    if (i == rowIndex || cheats.get(i).getDescription()
                            .startsWith(prefix)) {
                        cheats.get(i).setEnabled((Boolean) value);
                        fireTableRowsUpdated(i, i);
                    }
                }
                return;
            }
        }

        if (columnIndex == 0) {
            cheat.setEnabled((boolean) value);
        } else {
            cheat.setDescription((String) value);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    @Override
    public String getColumnName(final int columnIndex) {
        if (columnIndex == 0) {
            return "";
        } else {
            return "Description";
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 0;
    }

    public void clear() {
        modified = true;
        cheats.clear();
        fireTableDataChanged();
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(final boolean modified) {
        this.modified = modified;
    }
}