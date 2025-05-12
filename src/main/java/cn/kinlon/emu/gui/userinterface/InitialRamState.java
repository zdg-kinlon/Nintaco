package cn.kinlon.emu.gui.userinterface;

public enum InitialRamState {

    All00("All $00"),
    AllFF("All $FF"),
    Random("Random");

    final String displayName;

    InitialRamState(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
