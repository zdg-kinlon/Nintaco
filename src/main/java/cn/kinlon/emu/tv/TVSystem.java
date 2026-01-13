package cn.kinlon.emu.tv;

public record TVSystem(
        double cyclesPerSecond,
        double secondsPerCycle,
        double cyclesPerFrame,
        double framesPerCycle,
        double framesPerSecond,
        double secondsPerFrame,
        long nanosPerFrame,
        int scanlineCount,
        int nmiScanline,
        PixelAspectRatio pixelAspectRatio,
        ScreenBorders screenBorders
) {
    public static final TVSystem NTSC = new TVSystem(
            19687500.0 / 11.0,
            11.0 / 19687500.0,
            59561.0 / 2.0,
            2.0 / 59561.0,
            39375000.0 / 655171.0,
            655171.0 / 39375000.0,
            Math.round(1.0E9 * 655171.0 / 39375000.0),
            262,
            241,
            new PixelAspectRatio(8, 7),
            new ScreenBorders(8, 8, 0, 0)
    );

    public static final TVSystem PAL = new TVSystem(
            53203425.0 / 32.0,
            32.0 / 53203425.0,
            66495.0 / 2.0,
            2.0 / 66495.0,
            322445.0 / 6448.0,
            6448.0 / 322445.0,
            Math.round(1.0E9 * 6448.0 / 322445.0),
            312,
            241,
            new PixelAspectRatio(2950000, 2128137)
            , new ScreenBorders(1, 0, 2, 2)
    );
    public static final TVSystem Dendy = new TVSystem(
            3546895.0 / 2.0,
            2.0 / 3546895.0,
            35464.0,
            1.0 / 35464.0,
            322445.0 / 6448.0,
            6448.0 / 322445.0,
            Math.round(1.0E9 * 6448.0 / 322445.0),
            312,
            291,
            new PixelAspectRatio(2950000, 2128137)
            , new ScreenBorders(1, 0, 2, 2)
    );
}
