package nintaco.gui.image.filters;

public class DuplicateFilterFactory implements VideoFilterArrayFactory {

    protected final VideoFilterFactory factory;

    public DuplicateFilterFactory(final VideoFilterFactory factory) {
        this.factory = factory;
    }

    @Override
    public VideoFilter[] createFilters(final int count,
                                       final int[][] extendedPalettes) {
        final VideoFilter filter = factory.createFilter();
        final VideoFilter[] filters = new VideoFilter[count];
        for (int i = count - 1; i >= 0; i--) {
            filters[i] = filter;
        }
        return filters;
    }
}
