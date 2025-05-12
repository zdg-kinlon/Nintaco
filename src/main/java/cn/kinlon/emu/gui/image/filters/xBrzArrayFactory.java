package cn.kinlon.emu.gui.image.filters;

public class xBrzArrayFactory implements VideoFilterArrayFactory {

    final xBRZ.ScaleSize scaleSize;

    public xBrzArrayFactory(final xBRZ.ScaleSize scaleSize) {
        this.scaleSize = scaleSize;
    }

    @Override
    public xBRZ[] createFilters(final int count,
                                final int[][] extendedPalettes) {
        final xBRZ[] filters = new xBRZ[count];
        filters[0] = new xBRZ(scaleSize);
        for (int i = count - 1; i > 0; i--) {
            filters[i] = new xBRZ(filters[0]);
        }
        return filters;
    }
}
