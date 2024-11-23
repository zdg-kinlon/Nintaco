package nintaco.api;

/**
 * The listener interface for receiving scanline cycle events. The class that
 * is interested in processing scanline cycle events implements this
 * interface and the object created with that class is registered with {@link
 * API#addScanlineCycleListener(nintaco.api.ScanlineCycleListener, int, int)
 * API.addScanlineCycleListener()}.
 */
public interface ScanlineCycleListener {

    /**
     * Invoked during the scanline and cycle specified in {@link
     * API#addScanlineCycleListener(nintaco.api.ScanlineCycleListener, int, int)
     * API.addScanlineCycleListener()}.
     *
     * @param scanline      The scanline index. -1 indicates the pre-render scanline.
     * @param scanlineCycle The scanline cycle index (the dot number), a value in
     *                      the range [0, 340].
     * @param address       The PPU Memory address accessed during the scanline cycle.
     * @param rendering     {@code true} if and only if the scanline index is
     *                      less-than 240 and either background or sprite rendering has been enabled
     *                      by writing to the PPUMASK register ({@code $2001}).
     */
    void cyclePerformed(int scanline, int scanlineCycle, int address,
                        boolean rendering);
}
