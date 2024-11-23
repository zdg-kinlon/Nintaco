package nintaco.gui.cheats;

import nintaco.cheats.Cheat;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheatsTableModel extends AbstractTableModel {

    public static final Pattern MULTI_PATTERN = Pattern
            .compile("^(.*)(\\[\\d+\\s+of\\s+\\d+\\])$");
    static final String[] COLUMN_NAMES = {"", "Description", "Address",
            "Value", "Compare", "Game Genie", "Pro Action Rocky"};
    private final List<CheatRow> cheatRows;
    private boolean showHex;
    private boolean modified;
    public CheatsTableModel(final List<Cheat> cheats) {
        this.cheatRows = new ArrayList<>();
        for (final Cheat cheat : cheats) {
            final CheatRow row = new CheatRow(cheat);
            row.setShowHex(showHex);
            cheatRows.add(row);
            modified = true;
        }
    }

    public List<Cheat> getCheats() {
        final List<Cheat> cheats = new ArrayList<>();
        for (CheatRow row : cheatRows) {
            cheats.add(new Cheat(row.getCheat()));
        }
        return cheats;
    }

    public void setShowHex(final boolean showHex) {
        this.showHex = showHex;
        for (final CheatRow row : cheatRows) {
            row.setShowHex(showHex);
        }
        fireTableDataChanged();
    }

    public void clear() {
        modified = true;
        cheatRows.clear();
        fireTableDataChanged();
    }

    public void deleteCheat(final int rowIndex) {
        modified = true;
        final Matcher matcher = MULTI_PATTERN.matcher((String) cheatRows
                .get(rowIndex).getValueAt(Indices.DESCRIPTION));
        if (matcher.find()) {
            final String prefix = matcher.group(1);
            for (int i = cheatRows.size() - 1; i >= 0; i--) {
                if (i == rowIndex || ((String) cheatRows.get(i)
                        .getValueAt(Indices.DESCRIPTION)).startsWith(prefix)) {
                    cheatRows.remove(i);
                    fireTableRowsDeleted(i, i);
                }
            }
            return;
        }
        cheatRows.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void updateCheat(final Cheat cheat, final int rowIndex) {
        modified = true;
        final CheatRow row = new CheatRow(cheat);
        row.setShowHex(showHex);
        cheatRows.set(rowIndex, row);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void addCheat(final Cheat cheat) {
        addCheat(cheat, true, false);
    }

    private void addCheat(final Cheat cheat, final boolean fireChangedEvent,
                          final boolean removeDuplicates) {
        modified = true;
        if (removeDuplicates) {
            for (int i = cheatRows.size() - 1; i >= 0; i--) {
                final Cheat c = cheatRows.get(i).getCheat();
                if (c.effectivelyEquals(cheat)) {
                    cheatRows.remove(i);
                }
            }
        }
        final CheatRow row = new CheatRow(cheat);
        row.setShowHex(showHex);
        cheatRows.add(row);
        if (fireChangedEvent) {
            fireTableDataChanged();
        }
    }

    public void addCheats(final List<Cheat> cheats,
                          final boolean removeDuplicates) {
        for (final Cheat cheat : cheats) {
            addCheat(cheat, false, removeDuplicates);
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return cheatRows.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    public CheatRow getRow(int rowIndex) {
        return cheatRows.get(rowIndex);
    }

    public Cheat getCheat(int rowIndex) {
        return cheatRows.get(rowIndex).getCheat();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return cheatRows.get(rowIndex).getValueAt(columnIndex);
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex,
                           final int columnIndex) {

        modified = true;
        if (columnIndex == Indices.ENABLED) {
            final Matcher matcher = MULTI_PATTERN.matcher((String) cheatRows
                    .get(rowIndex).getValueAt(Indices.DESCRIPTION));
            if (matcher.find()) {
                final String prefix = matcher.group(1);
                for (int i = cheatRows.size() - 1; i >= 0; i--) {
                    if (i == rowIndex || ((String) cheatRows.get(i)
                            .getValueAt(Indices.DESCRIPTION)).startsWith(prefix)) {
                        cheatRows.get(i).setValueAt(value, Indices.ENABLED);
                        fireTableRowsUpdated(i, i);
                    }
                }
                return;
            }
        }
        cheatRows.get(rowIndex).setValueAt(value, columnIndex);
        fireTableRowsUpdated(rowIndex, rowIndex);
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
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(final boolean modified) {
        this.modified = modified;
    }

    interface Indices {
        int ENABLED = 0;
        int DESCRIPTION = 1;
        int ADDRESS = 2;
        int VALUE = 3;
        int COMPARE = 4;
        int GAME_GENIE = 5;
        int PRO_ACTION_ROCKY = 6;
    }
}

