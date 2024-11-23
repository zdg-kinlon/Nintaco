package nintaco.tv;

public enum TVSystem {

    NTSC(19687500.0 / 11.0,  // cyclesPerSecond
            11.0 / 19687500.0,     // secondsPerCycle
            59561.0 / 2.0,         // cyclesPerFrame
            2.0 / 59561.0,         // framesPerCycle
            39375000.0 / 655171.0, // framesPerSecond
            655171.0 / 39375000.0, // secondsPerFrame
            (long) Math.round(1.0E9 * 655171.0 / 39375000.0), // nanosPerFrame
            262,                   // scanlineCount
            241,                   // nmiScanline
            new PixelAspectRatio(8, 7),
            new ScreenBorders(8, 8, 0, 0)),

    PAL(53203425.0 / 32.0,   // cyclesPerSecond
            32.0 / 53203425.0,     // secondsPerCycle
            66495.0 / 2.0,         // cyclesPerFrame
            2.0 / 66495.0,         // framesPerCycle
            322445.0 / 6448.0,     // framesPerSecond
            6448.0 / 322445.0,     // secondsPerFrame
            (long) Math.round(1.0E9 * 6448.0 / 322445.0), // nanosPerFrame
            312,                   // scanlineCount
            241,                   // nmiScanline
            new PixelAspectRatio(2950000, 2128137),
            new ScreenBorders(1, 0, 2, 2)),

    Dendy(3546895.0 / 2.0,   // cyclesPerSecond
            2.0 / 3546895.0,       // secondsPerCycle
            35464.0,               // cyclesPerFrame
            1.0 / 35464.0,         // framesPerCycle
            322445.0 / 6448.0,     // framesPerSecond
            6448.0 / 322445.0,     // secondsPerFrame
            (long) Math.round(1.0E9 * 6448.0 / 322445.0), // nanosPerFrame
            312,                   // scanlineCount
            291,                   // nmiScanline 
            new PixelAspectRatio(2950000, 2128137),
            new ScreenBorders(1, 0, 2, 2));

    private final double cyclesPerSecond;
    private final double secondsPerCycle;
    private final double cyclesPerFrame;
    private final double framesPerCycle;
    private final double framesPerSecond;
    private final double secondsPerFrame;
    private final long nanosPerFrame;
    private final int scanlineCount;
    private final int nmiScanline;
    private final PixelAspectRatio pixelAspectRatio;
    private final ScreenBorders screenBorders;

    private TVSystem(
            final double cyclesPerSecond,
            final double secondsPerCycle,
            final double cyclesPerFrame,
            final double framesPerCycle,
            final double framesPerSecond,
            final double secondsPerFrame,
            final long nanosPerFrame,
            final int scanlineCount,
            final int nmiScanline,
            final PixelAspectRatio pixelAspectRatio,
            final ScreenBorders screenBorders) {
        this.cyclesPerSecond = cyclesPerSecond;
        this.secondsPerCycle = secondsPerCycle;
        this.cyclesPerFrame = cyclesPerFrame;
        this.framesPerCycle = framesPerCycle;
        this.framesPerSecond = framesPerSecond;
        this.secondsPerFrame = secondsPerFrame;
        this.nanosPerFrame = nanosPerFrame;
        this.scanlineCount = scanlineCount;
        this.nmiScanline = nmiScanline;
        this.pixelAspectRatio = pixelAspectRatio;
        this.screenBorders = screenBorders;
    }

    public double getCyclesPerSecond() {
        return cyclesPerSecond;
    }

    public double getSecondsPerCycle() {
        return secondsPerCycle;
    }

    public double getCyclesPerFrame() {
        return cyclesPerFrame;
    }

    public double getFramesPerCycle() {
        return framesPerCycle;
    }

    public double getFramesPerSecond() {
        return framesPerSecond;
    }

    public double getSecondsPerFrame() {
        return secondsPerFrame;
    }

    public long getNanosPerFrame() {
        return nanosPerFrame;
    }

    public int getScanlineCount() {
        return scanlineCount;
    }

    public int getNmiScanline() {
        return nmiScanline;
    }

    public PixelAspectRatio getPixelAspectRatio() {
        return pixelAspectRatio;
    }

    public ScreenBorders getScreenBorders() {
        return screenBorders;
    }
}
