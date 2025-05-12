package cn.kinlon.emu.gui.image.filters;

public class NesNtscArrayFactory implements VideoFilterArrayFactory {

    @Override
    public NesNtsc[] createFilters(final int count,
                                   final int[][] extendedPalettes) {
        final NesNtsc[] filters = new NesNtsc[count];
        filters[0] = new NesNtsc(extendedPalettes);
        for (int i = count - 1; i > 0; i--) {
            filters[i] = new NesNtsc(filters[0]);
        }
        return filters;
    }
}
