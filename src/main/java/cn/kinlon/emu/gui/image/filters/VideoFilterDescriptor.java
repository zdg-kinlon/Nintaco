package cn.kinlon.emu.gui.image.filters;

public enum VideoFilterDescriptor {

    Current("[current]", -1, (DuplicateFilterFactory) null),
    NoFilter("No Filter", 2, (DuplicateFilterFactory) null),
    Scale2x("Scale2x", 2, Scale2x::new),
    Scale3x("Scale3x", 3, Scale3x::new),
    Scale4x("Scale4x", 4, Scale4x::new),
    LQ2x("LQ2x", 2, LQ2x::new),
    LQ3x("LQ3x", 3, LQ3x::new),
    LQ4x("LQ4x", 4, LQ4x::new),
    LQ5x("LQ5x", 5, LQ5x::new),
    _2xBRZ("2xBRZ", 2, new xBrzArrayFactory(xBRZ.ScaleSize.Times2)),
    _3xBRZ("3xBRZ", 3, new xBrzArrayFactory(xBRZ.ScaleSize.Times3)),
    _4xBRZ("4xBRZ", 4, new xBrzArrayFactory(xBRZ.ScaleSize.Times4)),
    _5xBRZ("5xBRZ", 5, new xBrzArrayFactory(xBRZ.ScaleSize.Times5)),
    Retro3x("Retro3x", 3, new Retro3xArrayFactory(), true, true),
    Scanlines3x("Scanlines3x", 3, new ScanlinesArrayFactory(3), true, true),
    Scanlines4x("Scanlines4x", 4, new ScanlinesArrayFactory(4), true, true),
    Scanlines5x("Scanlines5x", 5, new ScanlinesArrayFactory(5), true, true),
    Ntsc("NTSC2x", 2, new NesNtscArrayFactory(), true, true, 602, 240);

    private final String name;
    private final int scale;
    private final VideoFilterArrayFactory factory;
    private final boolean smoothScaling;
    private final boolean useTvAspectRatio;
    private final int width;
    private final int height;

    VideoFilterDescriptor(final String name, final int scale,
                          final VideoFilterFactory factory) {
        this(name, scale,
                factory == null ? null : new DuplicateFilterFactory(factory));
    }

    VideoFilterDescriptor(final String name, final int scale,
                          final VideoFilterArrayFactory arrayFactory) {
        this(name, scale, arrayFactory, false, false);
    }

    VideoFilterDescriptor(final String name, final int scale,
                          final VideoFilterArrayFactory arrayFactory, final boolean smoothScaling,
                          final boolean useTvAspectRatio) {
        this(name, scale, arrayFactory, smoothScaling, useTvAspectRatio,
                scale << 8, 240 * scale);
    }

    VideoFilterDescriptor(final String name, final int scale,
                          final VideoFilterArrayFactory arrayFactory, final boolean smoothScaling,
                          final boolean useTvAspectRatio, final int width, final int height) {
        this.name = name;
        this.scale = scale;
        this.factory = arrayFactory;
        this.smoothScaling = smoothScaling;
        this.useTvAspectRatio = useTvAspectRatio;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public int getScale() {
        return scale;
    }

    public boolean isSmoothScaling() {
        return smoothScaling;
    }

    public boolean isUseTvAspectRatio() {
        return useTvAspectRatio;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public VideoFilter[] createFilters(final int count,
                                       final int[][] extendedPalettes) {
        return factory == null ? null : factory.createFilters(count,
                extendedPalettes);
    }

    @Override
    public String toString() {
        return getName();
    }
}