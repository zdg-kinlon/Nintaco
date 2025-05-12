package cn.kinlon.emu.gui.archive;

public class EntryElement {

    private String entry;
    private String entryHTML;

    public String getEntry() {
        return entry;
    }

    @Override
    public String toString() {
        return entryHTML;
    }
}
