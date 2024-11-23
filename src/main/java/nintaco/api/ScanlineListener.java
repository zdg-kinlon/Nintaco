package nintaco.api;

/**
 * The listener interface for receiving scanline render events. The class that
 * is interested in processing scanline render events implements this
 * interface and the object created with that class is registered with
 * {@link API#addScanlineListener(nintaco.api.ScanlineListener, int)
 * API.addScanlineListener()}.
 */
@FunctionalInterface
public interface ScanlineListener {

    /**
     * Invoked after rendering the scanline specified in {@link
     * API#addScanlineListener(nintaco.api.ScanlineListener, int)
     * API.addScanlineListener()}. This method will be called back during scanline
     * cycle 257, immediately after the PPU copies all bits related to horizontal
     * position from register <i>t</i> to register <i>v</i>.
     *
     * @param scanline The index of the rendered scanline. -1 refers to the
     *                 pre-render scanline.
     */
    void scanlineRendered(int scanline);
}
