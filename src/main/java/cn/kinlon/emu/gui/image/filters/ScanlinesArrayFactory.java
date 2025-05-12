package cn.kinlon.emu.gui.image.filters;

public class ScanlinesArrayFactory implements VideoFilterArrayFactory {

    private final int SCALE;

    public ScanlinesArrayFactory(final int scale) {
        this.SCALE = scale;
    }

    @Override
    public Scanlines[] createFilters(final int count,
                                     final int[][] extendedPalettes) {
        final Scanlines[] filters = new Scanlines[count];
        filters[0] = new Scanlines(SCALE);
        for (int i = count - 1; i > 0; i--) {
            filters[i] = new Scanlines(filters[0]);
        }
        return filters;
    }
}
