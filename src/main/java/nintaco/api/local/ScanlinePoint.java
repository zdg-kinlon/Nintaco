package nintaco.api.local;

import nintaco.api.ScanlineListener;

public class ScanlinePoint {

    public final ScanlineListener listener;
    public final int scanline;

    public ScanlinePoint(final ScanlineListener listener, final int scanline) {
        this.listener = listener;
        this.scanline = scanline;
    }

    public ScanlineListener getListener() {
        return listener;
    }

    public int getScanline() {
        return scanline;
    }
}
