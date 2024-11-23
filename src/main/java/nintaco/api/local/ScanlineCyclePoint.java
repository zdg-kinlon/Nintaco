package nintaco.api.local;

import nintaco.api.ScanlineCycleListener;

public class ScanlineCyclePoint {

    public final ScanlineCycleListener listener;
    public final int scanline;
    public final int scanlineCycle;

    public ScanlineCyclePoint(final ScanlineCycleListener listener,
                              final int scanline, final int scanlineCycle) {
        this.listener = listener;
        this.scanline = scanline;
        this.scanlineCycle = scanlineCycle;
    }

    public ScanlineCycleListener getListener() {
        return listener;
    }

    public int getScanline() {
        return scanline;
    }

    public int getScanlineCycle() {
        return scanlineCycle;
    }
}
