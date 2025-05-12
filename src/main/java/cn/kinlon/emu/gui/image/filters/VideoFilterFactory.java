package cn.kinlon.emu.gui.image.filters;

@FunctionalInterface
public interface VideoFilterFactory {
    VideoFilter createFilter();
}
