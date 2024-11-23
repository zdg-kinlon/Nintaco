package nintaco.gui.cheats.search;

import static nintaco.gui.cheats.search.RamTableModel.Indices.ADDRESS;
import static nintaco.gui.cheats.search.RamTableModel.Indices.R0;

public final class RamTableRow {

    public static final int DECIMAL = 0;
    public static final int HEXIDECIMAL = 1;

    private final String[][] values
            = new String[2][RamTableModel.COLUMN_NAMES.length];
    private final int[] r = new int[2];

    private int address;

    public RamTableRow() {
    }

    public RamTableRow(int address, int r0, int r1) {
        setAddress(address);
        setR(0, r0);
        setR(1, r1);
    }

    public RamTableRow(RamTableRow row) {
        this(row.address, row.r[0], row.r[1]);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
        values[DECIMAL][ADDRESS] = values[HEXIDECIMAL][ADDRESS]
                = String.format("%04X", address);
    }

    public int getR(int index) {
        return r[index];
    }

    public void setR(int index, int value) {
        r[index] = value;
        index += R0;
        values[HEXIDECIMAL][index] = String.format("%02X", value);
        values[DECIMAL][index] = Integer.toString(value);
    }

    public Object getValueAt(int columnIndex, int base) {
        return values[base][columnIndex];
    }
}
