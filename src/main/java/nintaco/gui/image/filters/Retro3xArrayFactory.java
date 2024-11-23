package nintaco.gui.image.filters;

public class Retro3xArrayFactory implements VideoFilterArrayFactory {

    @Override
    public Retro3x[] createFilters(final int count,
                                   final int[][] extendedPalettes) {
        final Retro3x[] filters = new Retro3x[count];
        filters[0] = new Retro3x();
        for (int i = count - 1; i > 0; i--) {
            filters[i] = new Retro3x(filters[0]);
        }
        return filters;
    }
}
