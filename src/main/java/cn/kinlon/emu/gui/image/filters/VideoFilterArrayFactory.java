package cn.kinlon.emu.gui.image.filters;

public interface VideoFilterArrayFactory {
    VideoFilter[] createFilters(int count, int[][] extendedPalettes);
}
