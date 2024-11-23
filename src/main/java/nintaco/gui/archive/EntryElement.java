package nintaco.gui.archive;

public class EntryElement {

    private String entry;
    private String entryHTML;

    public EntryElement() {
    }

    public EntryElement(final String entry, final String entryHTML) {
        this.entry = entry;
        this.entryHTML = entryHTML;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getEntryHTML() {
        return entryHTML;
    }

    public void setEntryHTML(String entryHTML) {
        this.entryHTML = entryHTML;
    }

    @Override
    public String toString() {
        return entryHTML;
    }
}
