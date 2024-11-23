package nintaco.gui.exportmedia.preferences;

public enum FramesOption {

    SaveAll("Save All"),
    Alternate("Alternate"),
    Interlace("Interlace"),
    Merge("Merge");

    private final String displayName;

    FramesOption(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
