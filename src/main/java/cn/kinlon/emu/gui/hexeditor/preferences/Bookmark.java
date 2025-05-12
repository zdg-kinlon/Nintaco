package cn.kinlon.emu.gui.hexeditor.preferences;

import java.io.Serializable;

public class Bookmark implements Serializable {

    private static final long serialVersionUID = 0;

    private final int dataSourceIndex;
    private final int address;
    private final String name;

    public Bookmark(int dataSourceIndex, int address, String name) {
        this.dataSourceIndex = dataSourceIndex;
        this.address = address;
        this.name = name;
    }

    public int getDataSourceIndex() {
        return dataSourceIndex;
    }

    public int getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        final Bookmark bookmark = (Bookmark) obj;
        return address == bookmark.address
                && dataSourceIndex == bookmark.dataSourceIndex;
    }
}
