package nintaco.gui.spritesaver;

public class Metasprite implements Comparable<Metasprite> {

    public final byte[] paletteIndices;
    public final int width;
    public final int height;
    public final int hash;
    public final int id;

    public int occurrences = 1;
    public Metasprite next;

    public Metasprite(final byte[] paletteIndices, final int width,
                      final int height, final int hash, final int id) {
        this.paletteIndices = paletteIndices;
        this.width = width;
        this.height = height;
        this.hash = hash;
        this.id = id;
    }

    @Override
    public int compareTo(Metasprite m) {
        if (id < m.id) {
            return -1;
        } else if (id > m.id) {
            return 1;
        } else {
            return 0;
        }
    }
}
