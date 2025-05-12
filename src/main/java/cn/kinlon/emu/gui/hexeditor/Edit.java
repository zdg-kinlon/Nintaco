package cn.kinlon.emu.gui.hexeditor;

public class Edit {

    private final int address;
    private final int[] values;

    public Edit(int address, int value) {
        this.address = address;
        this.values = new int[]{value};
    }

    public Edit(int address, int[] values) {
        this.address = address;
        this.values = values;
    }

    public int getAddress() {
        return address;
    }

    public int[] getValues() {
        return values;
    }
}
