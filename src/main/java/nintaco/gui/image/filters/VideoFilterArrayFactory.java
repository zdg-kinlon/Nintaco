package nintaco.gui.image.filters;

public interface VideoFilterArrayFactory {
    VideoFilter[] createFilters(int count, int[][] extendedPalettes);
}
