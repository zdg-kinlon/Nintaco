package nintaco.gui.cheats;

import nintaco.cheats.Cheat;
import nintaco.cheats.GameGenie;
import nintaco.cheats.ProActionRocky;

import static nintaco.gui.cheats.CheatsTableModel.Indices.*;
import static nintaco.util.StringUtil.*;

public final class CheatRow {

    private final String[] values = new String[
            CheatsTableModel.COLUMN_NAMES.length];

    private Cheat cheat;
    private boolean showHex;

    public CheatRow(final CheatRow cheatRow) {
        this(cheatRow.getCheat());
    }

    public CheatRow(final Cheat cheat) {
        this.cheat = new Cheat(cheat);
        update(false);
    }

    public String[] getValues() {
        return values;
    }

    public Cheat getCheat() {
        return cheat;
    }

    public boolean isShowHex() {
        return showHex;
    }

    public void setShowHex(final boolean showHex) {
        this.showHex = showHex;
        values[ADDRESS] = String.format("$%04X", cheat.getAddress());
        values[VALUE] = getByteString(showHex, cheat.getDataValue());
        values[COMPARE] = cheat.hasCompareValue()
                ? getByteString(showHex, cheat.getCompareValue()) : "-";
    }

    public void update(final boolean showHex) {
        setShowHex(showHex);
        values[GAME_GENIE] = replaceBlank(GameGenie.convert(cheat), "-");
        values[PRO_ACTION_ROCKY] = replaceBlank(ProActionRocky.convert(cheat), "-");
    }

    private String getByteString(final boolean showHex, final int value) {
        return showHex ? String.format("$%02X", value) : Integer.toString(value);
    }

    public Object getValueAt(final int columnIndex) {
        switch (columnIndex) {
            case ENABLED:
                return cheat.isEnabled();
            case DESCRIPTION:
                return cheat.getDescription();
            default:
                return values[columnIndex];
        }
    }

    private void updateCheat(final Cheat c) {
        if (c != null) {
            c.setEnabled(cheat.isEnabled());
            c.setDescription(cheat.getDescription());
            cheat = c;
            update(showHex);
        }
    }

    public void setValueAt(final Object value, final int columnIndex) {
        switch (columnIndex) {
            case ENABLED:
                cheat.setEnabled((boolean) value);
                break;
            case DESCRIPTION: {
                final String description = ((String) value).trim();
                if (isBlank(description)) {
                    cheat.generateDescription();
                } else {
                    cheat.setDescription(description);
                }
                break;
            }
            case ADDRESS: {
                final int v = parseInt(value, true, 0xFFFF);
                if (v >= 0) {
                    cheat.setAddress(v);
                    update(showHex);
                }
                break;
            }
            case VALUE: {
                final int v = parseInt(value, showHex, 0xFF);
                if (v >= 0) {
                    cheat.setDataValue(v);
                    update(showHex);
                }
                break;
            }
            case COMPARE: {
                cheat.setCompareValue(parseInt(value, showHex, 0xFF));
                update(showHex);
                break;
            }
            case GAME_GENIE: {
                final String newCode = ((String) value).trim();
                if (!newCode.equals(values[GAME_GENIE])) {
                    updateCheat(GameGenie.convert(newCode));
                }
                break;
            }
            case PRO_ACTION_ROCKY: {
                final String newCode = ((String) value).trim();
                if (!newCode.equals(values[PRO_ACTION_ROCKY])) {
                    updateCheat(ProActionRocky.convert((String) value));
                }
                break;
            }
        }
    }
}
