package nintaco.gui.mapmaker;

public class MapTile {

    private byte[] paletteIndices;
    private boolean edgeTile;

    public byte[] getPaletteIndices() {
        return paletteIndices;
    }

    public void setPaletteIndices(byte[] paletteIndices) {
        this.paletteIndices = paletteIndices;
    }

    public boolean isEdgeTile() {
        return edgeTile;
    }

    public void setEdgeTile(boolean edgeTile) {
        this.edgeTile = edgeTile;
    }
}
